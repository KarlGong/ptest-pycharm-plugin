__requires__ = 'ptest==1.2.0'
import sys
from pkg_resources import load_entry_point

if __name__ == '__main__':
    sys.exit(
        load_entry_point('ptest==1.2.0', 'console_scripts', 'ptest')()
    )
