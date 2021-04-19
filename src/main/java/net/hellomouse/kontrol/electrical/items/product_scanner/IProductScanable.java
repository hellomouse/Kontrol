package net.hellomouse.kontrol.electrical.items.product_scanner;

import net.minecraft.text.Text;

import java.util.ArrayList;

/**
 * Has additional information that can be read by
 * a product scanner
 * @author Bowserinator
 */
public interface IProductScanable {
    /**
     * Return the product information
     * @return List of product info lines
     */
    ArrayList<Text> productInfo();
}
