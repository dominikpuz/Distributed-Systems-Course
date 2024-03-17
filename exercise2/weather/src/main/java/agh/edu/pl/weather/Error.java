package agh.edu.pl.weather;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Error {
    private int errorCode;
    private String errorMessage;
}
