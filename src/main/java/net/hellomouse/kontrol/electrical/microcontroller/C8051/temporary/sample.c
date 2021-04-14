
sbit SS = 0x80 ;

int main() {
    P0MDOUT = 1;

    while (1) {
        SS = 1;
    }
}