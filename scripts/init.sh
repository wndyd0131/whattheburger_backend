# create schema
mysql -u root -p whattheburger_dev < src/main/resources/db/whattheburger_schema_dev_v4.sql
# insert category, option...
mysql -u root -p whattheburger_dev < src/main/resources/db/migration/R__dev_seed_data.sql
# insert store
mysql -u root -p whattheburger_dev < src/main/resources/db/migration/store.sql
# start app (add user, product, store_product)
# insert stock