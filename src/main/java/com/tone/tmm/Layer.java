package com.tone.tmm;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Layer {
    private Material material;
    private double thickness;// in nm
}
