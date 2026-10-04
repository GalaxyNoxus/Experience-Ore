import hashlib
import json
import os
from pathlib import Path
import re
import sys
import urllib.error
import urllib.parse
import urllib.request
import uuid
import zipfile
from prepare_release import targets, read_jar, version_number, artifact_filename

API = 'https://api.modrinth.com/v2'
USER_AGENT = 'GalaxyNoxus/experience-ore (https://github.com/GalaxyNoxus/experience-ore)'


def properties(path):
    return dict(line.strip().split('=', 1) for line in Path(path).read_text().splitlines()
                if '=' in line and not line.lstrip().startswith(('#', '!')))


def release_version():
    version = properties('gradle.properties')['mod_version']
    tag = os.environ.get('RELEASE_TAG', '')
    if not re.fullmatch(r'\d+\.\d+\.\d+(?:-(?:alpha|beta|rc)[.\w-]*)?', version):
        raise RuntimeError('Use a release version such as 1.0.0, 1.1.0-beta.1 or 1.1.0-rc.1.')
    if tag != 'v' + version:
        raise RuntimeError(f'Tag must be v{version}, matching mod_version in gradle.properties.')
    return version


def request(path, token, body=None, content_type=None):
    headers = {'User-Agent': USER_AGENT, 'Authorization': token}
    if content_type:
        headers['Content-Type'] = content_type
    req = urllib.request.Request(API + path, data=body, headers=headers,
                                 method='POST' if body is not None else 'GET')
    try:
        with urllib.request.urlopen(req, timeout=120) as response:
            return json.load(response)
    except urllib.error.HTTPError as error:
        detail = error.read().decode('utf-8', errors='replace').replace(token, '[REDACTED]')
        raise RuntimeError(f'Modrinth HTTP {error.code}: {detail[:1200]}') from None


def collect(version):
    directory = Path(os.environ.get('RELEASE_DIR', 'release'))
    manifest = json.loads((directory / 'release-manifest.json').read_text())
    if manifest.get('mod_id') != 'xpore' or manifest.get('mod_version') != version:
        raise RuntimeError('Release manifest does not match this project version.')
    expected = {(t['loader'], t['minecraft']) for t in targets()}
    covered = set()
    planned = []
    prefix = properties('gradle.properties')['archives_base_name']
    for item in manifest['artifacts']:
        loader, versions = item['loader'], item['game_versions']
        number = version_number(version, loader, versions)
        if (loader not in ['fabric', 'forge'] or not versions or len(versions) != len(set(versions))
                or item['version_number'] != number or item['file'] != artifact_filename(prefix, version, loader, versions)
                or Path(item['file']).name != item['file']):
            raise RuntimeError('Invalid release artifact.')
        jar = directory / item['file']
        digest = hashlib.sha512(jar.read_bytes()).hexdigest()
        if digest != item['sha512']:
            raise RuntimeError('Checksum mismatch: ' + jar.name)
        read_jar(jar, loader, versions, version)
        for mc in versions:
            key = (loader, mc)
            if key not in expected or key in covered:
                raise RuntimeError('Duplicate or unsupported target: ' + str(key))
            covered.add(key)
        planned.append((loader, versions, number, jar, digest))
    if covered != expected:
        raise RuntimeError('Release is missing build targets.')
    return planned


def multipart(metadata, jar):
    boundary = 'xpore-' + uuid.uuid4().hex
    parts = [f'--{boundary}\r\nContent-Disposition: form-data; name="data"\r\n'
             'Content-Type: application/json\r\n\r\n'.encode()]
    parts.append(json.dumps(metadata).encode())
    parts.append(f'\r\n--{boundary}\r\nContent-Disposition: form-data; name="file"; '
                 f'filename="{jar.name}"\r\nContent-Type: application/java-archive\r\n\r\n'.encode())
    parts.append(jar.read_bytes())
    parts.append(f'\r\n--{boundary}--\r\n'.encode())
    return b''.join(parts), f'multipart/form-data; boundary={boundary}'


def main():
    version = release_version()
    if '--validate-tag' in sys.argv:
        print(f'Release tag validated: v{version}')
        return
    token = os.environ.get('MODRINTH_TOKEN', '').strip()
    if not token:
        raise RuntimeError('Create the MODRINTH_TOKEN repository secret before publishing.')
    planned = collect(version)
    slug = os.environ.get('MODRINTH_PROJECT', 'xp-ore')
    project = request('/project/' + urllib.parse.quote(slug, safe=''), token)
    if project.get('project_type') != 'mod' or project.get('slug') != slug:
        raise RuntimeError('The destination must be the configured Modrinth mod project.')
    project_id = project['id']
    supported = {item['version'] for item in request('/tag/game_version', token)}
    missing = [mc for _, versions, _, _, _ in planned for mc in versions if mc not in supported]
    if missing:
        raise RuntimeError('Minecraft versions not recognized by Modrinth: ' + ', '.join(missing))
    fabric_id = request('/project/fabric-api', token)['id']
    existing = request(f'/project/{project_id}/version', token)
    pending = []
    for loader, versions, number, jar, digest in planned:
        matches = [v for v in existing if v['version_number'] == number]
        if matches:
            if (len(matches) != 1 or set(matches[0]['game_versions']) != set(versions)
                    or matches[0]['loaders'] != [loader]
                    or not any(f['hashes'].get('sha512') == digest for f in matches[0]['files'])):
                raise RuntimeError(f'{number} already exists with different content. Use a new mod version.')
            print(f'Already uploaded, skipping: {number}')
        else:
            pending.append((loader, versions, number, jar))
    notes = Path('release-notes') / f'{version}.md'
    changelog = notes.read_text() if notes.exists() else f'Experience Ore {version} for Fabric and Forge.'
    channel = 'alpha' if '-alpha' in version else 'beta' if '-' in version else 'release'
    for loader, versions, number, jar in pending:
        metadata = {
            'name': f'Experience Ore {version} — {loader.title()}',
            'version_number': number,
            'project_id': project_id,
            'changelog': changelog,
            'dependencies': [{'project_id': fabric_id, 'dependency_type': 'required'}] if loader == 'fabric' else [],
            'game_versions': versions,
            'version_type': channel,
            'loaders': [loader],
            'featured': False,
            'status': 'listed',
            'file_parts': ['file'],
            'primary_file': 'file'
        }
        body, content_type = multipart(metadata, jar)
        result = request('/version', token, body, content_type)
        print(f'Uploaded {number}: https://modrinth.com/mod/{slug}/version/{result["id"]}')
    print(f'Complete: {len(planned)} release JARs checked; {len(pending)} versions uploaded.')
    print(f'Project status: {project["status"]}. Project review/public visibility is managed in Modrinth.')


if __name__ == '__main__':
    try:
        main()
    except Exception as error:
        message = str(error)
        token = os.environ.get('MODRINTH_TOKEN', '')
        if token:
            message = message.replace(token, '[REDACTED]')
        print(f'ERROR: {message}', file=sys.stderr)
        sys.exit(1)
