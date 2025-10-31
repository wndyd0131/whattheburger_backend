from sqlalchemy import create_engine, MetaData, Table
from sqlalchemy.dialects.mysql import insert as mysql_insert

import os
from OSMPythonTools.overpass import overpassQueryBuilder, Overpass
from OSMPythonTools.nominatim import Nominatim

db_user = os.environ.get("SPRING_DATASOURCE_USERNAME", "root")
db_password = os.environ.get("SPRING_DATASOURCE_PASSWORD", "1234")
db_host = os.environ.get("SPRING_DATASOURCE_HOST", "localhost")
db_port = os.environ.get("SPRING_DATASOURCE_PORT", "3306")
db_name = os.environ.get("SPRING_DATASOURCE_NAME", "whattheburger_dev")

db_url = f"mysql+pymysql://{db_user}:{db_password}@{db_host}:{db_port}/{db_name}"

print(db_url)

engine = create_engine(db_url)

nominatim = Nominatim()

query = overpassQueryBuilder(area=nominatim.query('USA'), elementType='way', selector=['"brand"="Whataburger"', '"amenity"="fast_food"'], out='center', includeCenter=True, includeGeometry=True)
overpass = Overpass()

whataburger = overpass.query(query, timeout=60)

wbs = whataburger.elements()
wb_set = set() # using set for duplicate check
wb_rows = []
for wb in wbs:
    overpass_id = wb.id()
    if overpass_id not in wb_set:
        latitude = wb.centerLat()
        longitude = wb.centerLon()
        city = wb.tag("addr:city")
        house_number = wb.tag("addr:housenumber")
        zipcode = wb.tag("addr:postcode")
        state = wb.tag("addr:state")
        street = wb.tag("addr:street")
        branch = wb.tag("branch")
        # opening_hours = wb.tag("opening_hours")
        phone_num = wb.tag("phone")
        website = wb.tag("website")
        wb_rows.append({
            "overpass_id": overpass_id,
            "latitude": latitude,
            "longitude": longitude,
            "city": city,
            "house_number": house_number,
            "zipcode": zipcode,
            "state": state,
            "street": street,
            "branch": branch,
            "phone_num": phone_num,
            "website": website
        })
        wb_set.add(overpass_id)


meta = MetaData()
store_tbl = Table("store", meta, autoload_with=engine)
with engine.begin() as conn:
    stmt = mysql_insert(store_tbl).values(wb_rows)
    upsert = stmt.on_duplicate_key_update(
        overpass_id=stmt.inserted.overpass_id,
        latitude=stmt.inserted.latitude,
        longitude=stmt.inserted.longitude,
        city=stmt.inserted.city,
        house_number=stmt.inserted.house_number,
        zipcode=stmt.inserted.zipcode,
        state=stmt.inserted.state,
        street=stmt.inserted.street,
        branch=stmt.inserted.branch,
        phone_num=stmt.inserted.phone_num,
        website=stmt.inserted.website
    )
    conn.execute(upsert)