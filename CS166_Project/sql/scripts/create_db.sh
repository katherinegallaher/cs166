#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
psql -p 6400 projectdb < $DIR/../src/create_tables.sql
psql -p 6400 projectdb < $DIR/../src/create_index.sql
psql -p 6400 projectdb < $DIR/../src/load_data.sql
