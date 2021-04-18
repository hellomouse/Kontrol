package net.hellomouse.kontrol.electrical.items.multimeters;

/**
 * Contains data that can be read by a multimeter
 * @author Bowserinator
 */
public interface IMultimeterReadable {
    /**
     * Return a multimeter reading
     * @return Multimeter reading
     */
    MultimeterReading getReading();
}
