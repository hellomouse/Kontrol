"""
Functions for automatically generating mod assets
"""

import os
import shutil


ASSETS_ROOT = "../src/main/resources/assets/kontrol/"

CABLE_REPLACE = "[CABLE]"

COLOR_REPLACE = "[[COLOR]]"
COLORS = "white, red, orange, pink, yellow, lime, green, light_blue, cyan, blue, magenta, purple, brown, gray, light_gray, black".split(", ")


def generate_colored_block_jsons(copy_target, target_dir, name, modifier=lambda x: x):
    """
    Copy a template to a destination directory for all colored variants

    :param copy_target: Template file path to copy
    :param target_dir: Destination directory
    :param name: Identifier for the model w/o namespace prefix, ie basic_cable
    :param modifier: Function to modify template contents before writing
    :return: None
    """
    with open(copy_target, "r") as f1:
        data = f1.read()

        for color in COLORS:
            _name = color + "_" + name + ".json"
            with open(os.path.join(target_dir, _name), "w") as f2:
                f2.write(modifier(data.replace(COLOR_REPLACE, color)))


def create_cable(model_dir, name, size):
    """
    Create a cable model using parts from model_dir, saves output to
    src/main/resources/assets/kontrol/models/block/cables/[name]

    :param name: Identifier for cable without namespace, ie basic_cable
    :param model_dir: Directory for the model parts. This directory must contain
                      two model jsons called middle.json and connector.json, for the
                      middle part and a side part respectively
    :param size: Size of cable (width / 2), same as size in the java code
    :return: None
    """

    destination_dir = os.path.join(ASSETS_ROOT, "models/block/cables/" + name)
    for model_part in ["middle", "connector"]:
        destination_file = os.path.join(destination_dir, model_part + ".json")
        
        if not os.path.exists(destination_dir):
            os.makedirs(destination_dir)

        shutil.copy2(os.path.join(model_dir, "{}.json".format(model_part)), destination_file)

    destination_dir = os.path.join(ASSETS_ROOT, "blockstates")
    generate_colored_block_jsons("./autogen-template/blockstates/cable_template.json", destination_dir, name, lambda x: x.replace(CABLE_REPLACE, name))

    destination_dir = os.path.join(ASSETS_ROOT, "models/item")
    x1 = str(8 - size)
    x2 = str(8 + size)
    texture = "kontrol:block/cables/" + name
    generate_colored_block_jsons("./autogen-template/models/item/cable_template.json", destination_dir, name, lambda x: x.replace(CABLE_REPLACE, name).replace("[TEXTURE]", texture).replace("[X1]", x1).replace("[X2]", x2))


    # generate_colored_block_jsons("./model_item/basic_cable.json", "../src/main/resources/assets/kontrol/models/item", "basic_cable")

