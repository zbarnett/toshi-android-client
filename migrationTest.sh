#!/bin/bash
# This script will check that database migration is correctly configured

LATEST_MIGRATION=$(grep -o 'oldVersion == [0-9]\{1,3\}' app/src/main/java/com/toshi/manager/store/DbMigration.java | tail -1 | cut -d' ' -f 3)
echo "Latest migration in DbMigration is " $LATEST_MIGRATION

DB_VERSION=$(grep -o 'schemaVersion([0-9]\{1,3\}' app/src/main/java/com/toshi/manager/ToshiManager.java | cut -d'(' -f 2)
echo "DB version in ToshiManager is " $DB_VERSION

EXPECTED_DB_VERSION="$(($LATEST_MIGRATION + 1))"
if [[ $EXPECTED_DB_VERSION == $DB_VERSION ]]; then
    echo "All good!"
else
    echo "ERROR: Missing database migration or schemaVersion is incorrect"
    exit 1
fi
