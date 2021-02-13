# Run in dev folder
import os

# Must be run in dev, if not try to cd to dev
if os.path.basename(os.path.normpath(os.getcwd())) != "dev":
    try:
        os.chdir("./dev")
    except FileNotFoundError as e:
        print("Script must be either run in the topmost of the Kontrol folder")
        print("or run in the Kontrol/dev folder")
        print("Please change ur CWD and check if Kontrol/dev exists")
        print("")
        raise e
        
REPLACE_STRING = "[[COLOR]]"
colors = "white, red, orange, pink, yellow, lime, green, light_blue, cyan, blue, magenta, purple, brown, gray, light_gray, black".split(", ")

"""
copy_target - Template file path ie /my/dev/stuff/wire.json
target_dir  - Directory to output to
name        - Base name of file w/o colors
"""
def generate_colored_block_jsons(copy_target, target_dir, name):
    with open(os.path.join(copy_target), "r") as f1:
        data = f1.read()
        
        for color in colors:
            n = color + "_" + name + ".json"
            with open(os.path.join(target_dir, n), "w") as f2:
                f2.write(data.replace(REPLACE_STRING, color))


generate_colored_block_jsons("./blockstates/basic_wire.json", "../src/main/resources/assets/kontrol/blockstates", "basic_wire")
generate_colored_block_jsons("./model_item/basic_wire.json", "../src/main/resources/assets/kontrol/models/item", "basic_wire")
generate_colored_block_jsons("./recipes/basic_wire.json", "../src/main/resources/data/kontrol/recipes", "basic_wire")
generate_colored_block_jsons("./loot_tables/basic_wire.json", "../src/main/resources/data/kontrol/loot_tables/blocks", "basic_wire")
