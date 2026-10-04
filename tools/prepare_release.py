import argparse
from collections import defaultdict
import hashlib
import json
from pathlib import Path
import re
import tomllib
import zipfile


def properties(path='gradle.properties'):
    return dict(line.strip().split('=', 1) for line in Path(path).read_text().splitlines()
                if '=' in line and not line.lstrip().startswith(('#', '!')))


def targets():
    result = [{'loader': 'fabric', 'minecraft': mc} for mc in json.loads(Path('targets.json').read_text())]
    result += [{'loader': 'forge', **t} for t in json.loads(Path('forge-targets.json').read_text())]
    keys = [(t['loader'], t['minecraft']) for t in result]
    if not keys or len(keys) != len(set(keys)):
        raise ValueError('Targets must be nonempty and unique.')
    for target in result:
        if target['loader'] == 'forge':
            latest = tuple(map(int, target['forge'].split('.')))
            minimum = tuple(map(int, target.get('minimumForge', target['forge']).split('.')))
            if minimum[0] != latest[0] or minimum > latest:
                raise ValueError('Invalid minimum Forge version: ' + target['minecraft'])
    return result


def canonical_manifest(data):
    sections = data.replace(b'\r\n', b'\n').replace(b'\n ', b'').strip().split(b'\n\n')
    skip = (b'Build-Time:', b'Built-By:', b'Created-By:', b'Stonecutter-Node-Project:',
            b'Stonecutter-Node-Version:', b'Fabric-Minecraft-Version:')
    result = []
    for index, section in enumerate(sections):
        lines = [line for line in section.splitlines() if not line.startswith(skip)]
        if index == 0:
            lines.sort(key=lambda line: (not line.startswith(b'Manifest-Version:'), line))
        for line in lines:
            while len(line) > 70:
                result.append(line[:70])
                line = b' ' + line[70:]
            result.append(line)
        result.append(b'')
    return b'\r\n'.join(result) + b'\r\n'


def api_version(value):
    match = re.fullmatch(r'>=(\d+)\.(\d+)\.(\d+)(?:\+[^ ]+)?', value)
    if not match:
        raise ValueError('Unexpected Fabric API dependency: ' + value)
    return tuple(map(int, match.groups()))


def read_jar(path, loader, versions, version):
    versions = [versions] if isinstance(versions, str) else versions
    with zipfile.ZipFile(path) as archive:
        names = [n for n in archive.namelist() if not n.endswith('/')]
        if len(names) != len(set(names)) or any(re.search(r'META-INF/[^/]+\.(SF|RSA|DSA)$', n, re.I) for n in names):
            raise ValueError('Duplicate entries or signed JAR: ' + str(path))
        payload = {n: archive.read(n) for n in names}
    if loader == 'fabric':
        metadata = json.loads(payload.pop('fabric.mod.json'))
        declared = metadata.get('depends', {}).get('minecraft')
        declared = [declared] if isinstance(declared, str) else declared
        if ('META-INF/mods.toml' in payload or metadata.get('id') != 'xpore'
                or metadata.get('version') != version or declared != versions):
            raise ValueError('Incorrect Fabric metadata: ' + str(path))
        fingerprint_metadata = json.loads(json.dumps(metadata))
        for key in ['minecraft', 'fabric-api']:
            fingerprint_metadata['depends'].pop(key)
        api_version(metadata['depends']['fabric-api'])
    elif loader == 'forge':
        if 'pack.mcmeta' not in payload or not json.loads(payload['pack.mcmeta']).get('pack'):
            raise ValueError('Forge resource pack metadata is missing: ' + str(path))
        metadata = tomllib.loads(payload.pop('META-INF/mods.toml').decode())
        mods = metadata.get('mods', [])
        deps = metadata.get('dependencies', {}).get('xpore', [])
        if ('fabric.mod.json' in payload or 'META-INF/neoforge.mods.toml' in payload
                or len(mods) != 1 or mods[0].get('modId') != 'xpore' or mods[0].get('version') != version
                or not any(d.get('modId') == 'minecraft' and d.get('versionRange') == ','.join(f'[{v}]' for v in versions) for d in deps)
                or not any(d.get('modId') == 'forge' and d.get('mandatory') is True for d in deps)):
            raise ValueError('Incorrect Forge metadata: ' + str(path))
        fingerprint_metadata = json.loads(json.dumps(metadata))
        fingerprint_metadata.pop('loaderVersion')
        for dep in fingerprint_metadata['dependencies']['xpore']:
            if dep['modId'] in ['minecraft', 'forge']:
                dep.pop('versionRange')
    else:
        raise ValueError('Unsupported loader: ' + loader)
    if 'META-INF/MANIFEST.MF' in payload:
        payload['META-INF/MANIFEST.MF'] = canonical_manifest(payload['META-INF/MANIFEST.MF'])
    digest = hashlib.sha256(json.dumps(fingerprint_metadata, sort_keys=True).encode())
    for name, data in sorted(payload.items()):
        digest.update(name.encode() + b'\0' + hashlib.sha256(data).digest())
    return metadata, payload, digest.hexdigest()


def toml_metadata(metadata):
    lines = [f'{key}={json.dumps(value, ensure_ascii=False)}' for key, value in metadata.items() if key not in ['mods', 'dependencies']]
    for mod in metadata['mods']:
        lines.append('[[mods]]')
        lines.extend(f'{key}={json.dumps(value, ensure_ascii=False)}' for key, value in mod.items())
    for owner, deps in metadata['dependencies'].items():
        for dep in deps:
            lines.append(f'[[dependencies.{owner}]]')
            lines.extend(f'{key}={json.dumps(value, ensure_ascii=False)}' for key, value in dep.items())
    return ('\n'.join(lines) + '\n').encode()


def merge_metadata(records, loader):
    metadata = json.loads(json.dumps(records[0]['metadata']))
    versions = [r['minecraft'] for r in records]
    if loader == 'fabric':
        metadata['depends']['minecraft'] = versions if len(versions) > 1 else versions[0]
        metadata['depends']['fabric-api'] = min((r['metadata']['depends']['fabric-api'] for r in records), key=api_version)
        return 'fabric.mod.json', (json.dumps(metadata, indent=2) + '\n').encode()
    minimum = min(int(r['metadata']['loaderVersion'].lstrip('[').split(',')[0]) for r in records)
    metadata['loaderVersion'] = f'[{minimum},)'
    for dep in metadata['dependencies']['xpore']:
        if dep['modId'] == 'minecraft':
            dep['versionRange'] = ','.join(f'[{mc}]' for mc in versions)
        elif dep['modId'] == 'forge':
            ranges = [next(d['versionRange'] for d in r['metadata']['dependencies']['xpore'] if d['modId'] == 'forge') for r in records]
            dep['versionRange'] = ','.join(dict.fromkeys(ranges))
    return 'META-INF/mods.toml', toml_metadata(metadata)


def minecraft_label(versions):
    if not versions or len(versions) != len(set(versions)):
        raise ValueError('Minecraft versions must be nonempty and unique.')
    if len(versions) == 1:
        return versions[0]
    return versions[0] + '_' + versions[-1]



def version_number(version, loader, versions):
    number = f'{version}-{loader}-{minecraft_label(versions)}'
    if len(number) > 32:
        raise ValueError('Modrinth version number exceeds 32 characters: ' + number)
    return number


def artifact_filename(prefix, version, loader, versions):
    return f'{prefix}-{version}-{loader}-mc{minecraft_label(versions)}.jar'


def prepare(input_dir, output_dir):
    props = properties()
    version, prefix = props['mod_version'], props['archives_base_name']
    buckets = defaultdict(list)
    for target in targets():
        loader, mc = target['loader'], target['minecraft']
        filename = artifact_filename(prefix, version, loader, [mc])
        found = list(input_dir.rglob(filename))
        if len(found) != 1:
            raise ValueError(f'Expected exactly one {filename}; found {len(found)}.')
        metadata, payload, fingerprint = read_jar(found[0], loader, mc, version)
        buckets[(loader, fingerprint)].append({'minecraft': mc, 'metadata': metadata, 'payload': payload,
            'source_sha512': hashlib.sha512(found[0].read_bytes()).hexdigest()})
    output_dir.mkdir(parents=True, exist_ok=True)
    if any(output_dir.iterdir()):
        raise ValueError('Release output must be empty. Choose a new output directory.')
    planned = []
    for (loader, fingerprint), records in buckets.items():
        versions = [r['minecraft'] for r in records]
        number = version_number(version, loader, versions)
        filename = artifact_filename(prefix, version, loader, versions)
        payload = dict(records[0]['payload'])
        key, data = merge_metadata(records, loader)
        payload[key] = data
        with zipfile.ZipFile(output_dir / filename, 'w', zipfile.ZIP_DEFLATED) as archive:
            for name, content in sorted(payload.items()):
                info = zipfile.ZipInfo(name, (1980, 1, 1, 0, 0, 0))
                info.compress_type = zipfile.ZIP_DEFLATED
                info.external_attr = 0o100644 << 16
                archive.writestr(info, content)
        read_jar(output_dir / filename, loader, versions, version)
        planned.append({'loader': loader, 'game_versions': versions, 'version_number': number, 'file': filename,
            'sha512': hashlib.sha512((output_dir / filename).read_bytes()).hexdigest(), 'payload_sha256': fingerprint,
            'sources': {r['minecraft']: r['source_sha512'] for r in records}})
    manifest = {'mod_id': 'xpore', 'mod_version': version, 'artifacts': planned}
    (output_dir / 'release-manifest.json').write_text(json.dumps(manifest, indent=2) + '\n')
    print(f'{len(targets())} builds -> {len(planned)} release JARs.')
    for item in planned:
        print(f'{item["loader"]}: {", ".join(item["game_versions"])} -> {item["file"]}')
    return manifest


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--input', type=Path, default=Path('build/raw'))
    parser.add_argument('--output', type=Path, default=Path('build/release'))
    args = parser.parse_args()
    prepare(args.input, args.output)
