-- Data generation
-- Category Create
insert into category (name) values ('burger'), ('all_time_favorite'), ('chicken'), ('breakfast'), ('kids'), ('group'), ('side'), ('salad'), ('dessert_and_snack'), ('drink_and_shake');

-- Whataburger Create
-- Product

-- Option Type
insert into option_type (name) values ('bread_type'),('beef_type'),('cheese_type'),('add_on'),('topping'),('sauce');
-- Option
insert into options (name, calories, option_type_id) values ('large_bun', 310, 1), ('small_bun', 170, 1), ('brioche_bun', 230, 1), ('texas_toast', 230, 1), ('no_bun', 0, 1);
insert into options (name, calories, option_type_id) values ('large_beef_patty', 240, 2);
insert into options (name, calories, option_type_id) values
                                                         ('american_cheese', 90, 3), ('monterey_jack_cheese', 60, 3), ('bacon_slices', 70, 4), ('grilled_jalapenos', 10, 4),
                                                         ('jalapenos', 0, 4), ('avocado', 90, 4), ('grilled_pepper_onion', 25, 4);
insert into options (name, calories, option_type_id) values ('tomato', 10, 5), ('lettuce', 0, 5), ('pickle', 0, 5), ('diced_onion', 10, 5), ('grilled_onion', 20, 5);
insert into options (name, calories, option_type_id) values ('mustard', 15, 6), ('mayonnaise', 50, 6), ('ketchup', 15, 6), ('honey_bbq', 60, 6), ('creamy_pepper', 60, 6);
-- Option Trait
insert into option_trait (name) values ('toast_both_side'), ('welldone'), ('melt'), ('easy_amount'), ('regular_amount'), ('extra_amount'), ('kid_size'), ('small_size'), ('medium_size'), ('large_size'), ('crispy'), ('saltless');
-- Product Option Trait
insert into product_option_trait (product_option_id, option_trait_id, is_default, extra_price, extra_calories) values (1, 1, false, 0, 0), (2, 1, false, 0, 0), (3, 1, false, 0, 0), (6, 2, false, 0, 0), (7, 3, false, 0, 0), (8, 3, false, 0, 0), (9, 11, false, 0, 0), (14, 4, false, 0, -10), (14, 5, true, 0, 0), (14, 6, false, 0, 5), (15, 4, false, 0, 0), (15, 5, true, 0, 0), (15, 6, false, 0, 5), (16, 4, false, 0, 0), (16, 5, true, 0, 0), (16, 6, false, 0, 0), (17, 4, false, 0, -10), (17, 5, true, 0, 0), (17, 6, false, 0, 5), (18, 4, false, 0, -20), (18, 5, true, 0, 0), (18, 6, false, 0, 5), (19, 4, false, 0, -10), (19, 5, true, 0, 0), (19, 6, false, 0, 5), (20, 4, false, 0, -25), (20, 5, true, 0, 0), (20, 6, false, 0, 30), (21, 4, false, 0, -5), (21, 5, true, 0, 0), (21, 6, false, 0, 10), (22, 4, false, 0, -30), (22, 5, true, 0, 0), (22, 6, false, 0, 20), (23, 4, false, 0, -30), (23, 5, true, 0, 0), (23, 6, false, 0, 30);


