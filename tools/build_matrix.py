import json
from prepare_release import targets

print('matrix=' + json.dumps({'include': targets()}, separators=(',', ':')))
