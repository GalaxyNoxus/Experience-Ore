import contextlib
import io
import json
import os
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
import zipfile
import prepare_release as package
import publish_modrinth as publish


class ReleaseTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.previous = Path.cwd()
        os.chdir(self.temp.name)
        Path('gradle.properties').write_text('mod_version=1.0.0\narchives_base_name=xp-ore\n')
        Path('targets.json').write_text('["1.20", "1.20.1"]')
        Path('forge-targets.json').write_text('[{"minecraft":"1.20","forge":"46.0.14"},{"minecraft":"1.20.1","forge":"47.4.26"}]')
        self.environment = patch.dict(os.environ, {'RELEASE_TAG': 'v1.0.0', 'RELEASE_DIR': 'release', 'MODRINTH_TOKEN': 'test-token'})
        self.environment.start()
        for target in package.targets():
            self.jar(target)

    def tearDown(self):
        self.environment.stop()
        os.chdir(self.previous)
        self.temp.cleanup()

    def jar(self, target, code=b'same-code', asset=b'same-asset'):
        mc, loader = target['minecraft'], target['loader']
        suffix = '-forge' if loader == 'forge' else ''
        path = Path(f'raw/{loader}/{mc}/xp-ore-1.0.0+mc{mc}{suffix}.jar')
        path.parent.mkdir(parents=True, exist_ok=True)
        if loader == 'fabric':
            name = 'fabric.mod.json'
            metadata = json.dumps({'id': 'xpore', 'version': '1.0.0', 'depends': {
                'minecraft': mc, 'fabric-api': '>=0.83.0+1.20', 'java': '>=17'}}).encode()
        else:
            name = 'META-INF/mods.toml'
            major = target['forge'].split('.')[0]
            metadata = package.toml_metadata({'modLoader': 'javafml', 'loaderVersion': f'[{major},)', 'license': 'MIT',
                'mods': [{'modId': 'xpore', 'version': '1.0.0'}], 'dependencies': {'xpore': [
                    {'modId': 'minecraft', 'mandatory': True, 'versionRange': f'[{mc}]'},
                    {'modId': 'forge', 'mandatory': True, 'versionRange': f'[{target["forge"]},{int(major)+1})'}]}})
        with zipfile.ZipFile(path, 'w') as archive:
            archive.writestr(name, metadata)
            if loader == 'forge':
                archive.writestr('pack.mcmeta', json.dumps({'pack': {'description': 'Experience Ore resources', 'pack_format': 15}}))
            archive.writestr('dev/galaxynoxus/xpore/XpOreMod.class', code)
            archive.writestr('assets/xpore/test.png', asset)
            archive.writestr('META-INF/MANIFEST.MF', f'Manifest-Version: 1.0\r\nFabric-Minecraft-Version: {mc}\r\nFabric-Mapping-Namespace: intermediary\r\n\r\n')
        return path

    def prepare(self):
        with contextlib.redirect_stdout(io.StringIO()):
            return package.prepare(Path('raw'), Path('release'))

    def test_merge_and_exact_loader_metadata(self):
        result = self.prepare()
        self.assertEqual(len(result['artifacts']), 2)
        for item in result['artifacts']:
            self.assertEqual(item['game_versions'], ['1.20', '1.20.1'])
        self.assertEqual(len(publish.collect('1.0.0')), 2)

    def test_missing_forge_pack_metadata_blocks_packaging(self):
        path = next(Path('raw').rglob('*-forge.jar'))
        with zipfile.ZipFile(path) as archive:
            entries = {name: archive.read(name) for name in archive.namelist() if name != 'pack.mcmeta'}
        with zipfile.ZipFile(path, 'w') as archive:
            for name, data in entries.items():
                archive.writestr(name, data)
        with self.assertRaisesRegex(ValueError, 'resource pack metadata'):
            self.prepare()

    def test_different_bytecode_not_merged(self):
        self.jar(package.targets()[1], code=b'changed-code')
        self.assertEqual(len(self.prepare()['artifacts']), 3)

    def test_different_resources_not_merged(self):
        self.jar(package.targets()[1], asset=b'changed-resource')
        self.assertEqual(len(self.prepare()['artifacts']), 3)

    def test_missing_target_blocks_packaging(self):
        next(Path('raw').rglob('*-forge.jar')).unlink()
        with self.assertRaises(ValueError):
            self.prepare()
        self.assertFalse(Path('release').exists())

    def test_checksum_mismatch_blocks_publishing(self):
        result = self.prepare()
        p = Path('release') / result['artifacts'][0]['file']
        p.write_bytes(p.read_bytes() + b'changed')
        with self.assertRaisesRegex(RuntimeError, 'Checksum'):
            publish.collect('1.0.0')

    def test_group_version_number_limit(self):
        versions = ['1.20', '1.20.1', '1.20.2', '1.20.3', '1.20.4']
        number = package.version_number('1.0.0', 'fabric', versions)
        self.assertLessEqual(len(number), 32)
        self.assertEqual(number, package.version_number('1.0.0', 'fabric', versions))
        self.assertNotEqual(number, package.version_number('1.0.0', 'forge', versions))
        self.assertNotEqual(number, package.version_number('1.0.0', 'fabric', versions[:-1]))

    def test_tag_must_match(self):
        with patch.dict(os.environ, {'RELEASE_TAG': 'v2.0.0'}):
            with self.assertRaises(RuntimeError):
                publish.release_version()

    def test_manifest_semantics_preserved(self):
        value = b'Manifest-Version: 1.0\r\nFabric-Loom-Client-Only-Entries: ' + b'x' * 160 + b'\r\nFabric-Mapping-Namespace: intermediary\r\n\r\n'
        normalized = package.canonical_manifest(value)
        self.assertTrue(normalized.startswith(b'Manifest-Version:'))
        self.assertTrue(all(len(line) <= 70 for line in normalized.split(b'\r\n')))
        self.assertIn(b'Fabric-Mapping-Namespace: intermediary', normalized)
        self.assertIn(b'x' * 160, normalized.replace(b'\r\n ', b''))

    def test_uploads_use_correct_loader_and_dependencies(self):
        self.prepare()
        uploaded = []
        def request(path, token, body=None, content_type=None):
            if path == '/project/xp-ore':
                return {'id': 'project', 'slug': 'xp-ore', 'project_type': 'mod', 'status': 'approved'}
            if path == '/tag/game_version':
                return [{'version': mc} for mc in ['1.20', '1.20.1']]
            if path == '/project/fabric-api':
                return {'id': 'fabric-api-id'}
            if path == '/project/project/version':
                return []
            if path == '/version':
                uploaded.append(json.loads(body))
                return {'id': str(len(uploaded))}
            raise AssertionError(path)
        with patch.object(publish, 'request', side_effect=request), patch.object(publish, 'multipart', side_effect=lambda meta, jar: (json.dumps(meta).encode(), 'application/json')), contextlib.redirect_stdout(io.StringIO()):
            publish.main()
        self.assertEqual(len(uploaded), 2)
        for item in uploaded:
            self.assertEqual(item['game_versions'], ['1.20', '1.20.1'])
            self.assertEqual(bool(item['dependencies']), item['loaders'] == ['fabric'])


if __name__ == '__main__':
    unittest.main()
