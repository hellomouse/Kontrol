# MUC Redstone Ports

### Description

In order to provide an interface between voltage levels and Minecraft's redstone power system, redstone ports are employed to both convert voltages into redstone signals and vice versa.



### Model numbering

All model numbers follow this naming scheme:

```
[B|D][I|O][L|G] [Digits]
  |    |    |       |_ The threshold. If the model is
  |    |    |          - An O model: the threshold is a voltage with precision to the tenths digit.
  |    |    |                        For instance, 12345 indicates a threshold voltage of 1234.5 V
  |    |    |          - An I model: the threshold is the redstone power, an integer from 0 to 15 inclusive.
  |    |    |
  |    |    |_ An O model: the threshold is a voltage with precision to the tenths digit.
  |    |
  |    |_ Does the port output a redstone signal (O) or take a redstone signal as input (I)
  |
  |_ Is the input/output binary (B) or discrete (D)?
```



## MUC Redstone Output Port - BOG07

**Dimensions:** 1m x 1m x 1m (1 full block)

**Behavior:** Outputs 15 RS power if input voltage > 0.7 V, otherwise outputs 0 RS power

**Internal circuit model:**

![](https://i.imgur.com/bK51aj9.png)

**Characteristics:**

| Characteristic            | Value |
| ------------------------- | ----- |
| Resistance (Ω)            | 1 MΩ  |
| Capacitance (F)           | 0.0   |
| Inductance (H)            | 0.0   |
| Max operating voltage (V) | TODO  |
| ThermalR (K/W)            | TODO  |
| ThermalC (J/W)            | TODO  |
| Hardness                  | 3.5   |
| Blast resistance          | 3.5   |

