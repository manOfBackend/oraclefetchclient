
load data

INFILE 'test_1GB.csv'

APPEND INTO TABLE adid_test_1GB

FIELDS TERMINATED BY ','
(idx,sha_key,manage_code,street_code,serial,is_under_ground,building_main_num,building_sub_num,basis_area_num,change_why_code,notice_date,before_street_code,has_detail_address,col0,col1,col2,col3,col4)
