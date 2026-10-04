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
from build_forge import compare_outputs
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
        path = Path('raw') / loader / mc / package.artifact_filename('xp-ore', '1.0.0', loader, [mc])
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
        path = next(Path('raw').rglob('*-forge-mc*.jar'))
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
        next(Path('raw').rglob('*-forge-mc*.jar')).unlink()
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

    def test_readable_release_identifiers(self):
        self.assertEqual(package.version_number('1.0.0', 'forge', ['1.20', '1.20.1']), '1.0.0-forge-1.20-1.20.1')
        self.assertEqual(package.artifact_filename('experience-ore', '1.0.0', 'forge', ['1.20', '1.20.1']),
                         'experience-ore-1.0.0-forge-mc1.20-1.20.1.jar')
        self.assertEqual(package.version_number('1.0.0', 'fabric', ['1.21.9', '1.21.10']), '1.0.0-fabric-1.21.9-1.21.10')

    def test_gaps_are_not_advertised_as_ranges(self):
        self.assertEqual(package.minecraft_label(['1.20', '1.20.2']), '1.20_1.20.2')

    def test_release_text_is_loader_specific(self):
        for loader, other in [('forge', 'Fabric'), ('fabric', 'Forge')]:
            name, changelog = publish.release_text('1.0.0', loader, ['1.20', '1.20.1'])
            self.assertIn(loader.title(), name)
            self.assertIn('1.20-1.20.1', name)
            self.assertNotIn(other, changelog)
            self.assertLessEqual(len(name), 64)
        self.assertIn('47.4.26', publish.release_text('1.0.0', 'forge', ['1.20.1'])[1])

    def test_all_46_individual_targets_have_single_version_names(self):
        for filename in ['targets.json', 'forge-targets.json']:
            Path(filename).write_text((self.previous / filename).read_text())
        configured = package.targets()
        self.assertEqual(len(configured), 46)
        for target in configured:
            self.jar(target, code=f"{target['loader']}:{target['minecraft']}".encode())
        result = self.prepare()
        self.assertEqual(len(result['artifacts']), 46)
        self.assertEqual(len(publish.collect('1.0.0')), 46)
        for item in result['artifacts']:
            self.assertEqual(len(item['game_versions']), 1)
            mc = item['game_versions'][0]
            self.assertEqual(item['file'], f"xp-ore-1.0.0-{item['loader']}-mc{mc}.jar")
            package.read_jar(Path('release') / item['file'], item['loader'], [mc], '1.0.0')

    def test_single_version_cannot_claim_a_group_in_manifest(self):
        for target in package.targets():
            self.jar(target, code=f"{target['loader']}:{target['minecraft']}".encode())
        manifest = self.prepare()
        item = manifest['artifacts'][0]
        item['game_versions'] = ['1.20', '1.20.1']
        item['version_number'] = package.version_number('1.0.0', item['loader'], item['game_versions'])
        renamed = package.artifact_filename('xp-ore', '1.0.0', item['loader'], item['game_versions'])
        (Path('release') / item['file']).rename(Path('release') / renamed)
        item['file'] = renamed
        Path('release/release-manifest.json').write_text(json.dumps(manifest))
        with self.assertRaisesRegex(ValueError, 'metadata'):
            publish.collect('1.0.0')

    def test_baseline_comparison_rejects_different_output(self):
        target = next(t for t in package.targets() if t['loader'] == 'forge')
        latest = self.jar(target)
        minimum = Path('minimum.jar')
        minimum.write_bytes(latest.read_bytes())
        compare_outputs(latest, minimum, target['minecraft'], '1.0.0')
        self.jar(target, code=b'new-api-dependent-output')
        with self.assertRaisesRegex(RuntimeError, 'builds differ'):
            compare_outputs(latest, minimum, target['minecraft'], '1.0.0')

    def test_minimum_forge_is_used_in_release_notes(self):
        data = json.loads(Path('forge-targets.json').read_text())
        data[1]['minimumForge'] = '47.4.10'
        Path('forge-targets.json').write_text(json.dumps(data))
        text = publish.release_text('1.0.0', 'forge', ['1.20.1'])[1]
        self.assertIn('47.4.10', text)
        self.assertNotIn('47.4.26', text)

    def test_minimum_must_be_same_major_and_not_above_latest(self):
        for minimum in ['46.0.14', '47.4.27']:
            data = json.loads(Path('forge-targets.json').read_text())
            data[1]['minimumForge'] = minimum
            Path('forge-targets.json').write_text(json.dumps(data))
            with self.assertRaisesRegex(ValueError, 'minimum Forge'):
                package.targets()

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
