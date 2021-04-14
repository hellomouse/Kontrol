
sbit SS = 0x80;
sbit IN = 0x83;

int main() {
    P0MDOUT |= 1; // Set pin 0.1 to output

    while (1) {
        SS = IN; // Set output = input
    }
}