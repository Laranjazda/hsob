package com.hsob.alura.TDD.service;

import com.hsob.alura.TDD.model.Funcionario;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BonusService {
    public Double calcularBonus(Funcionario funcionario) {
        Double valor = funcionario.getSalario() * 0.1;
        if (valor.compareTo(1000.0) > 0) {
            throw new IllegalArgumentException("Salário não compativel com a regra de bonus.");
        }
        return valor;
    }
}
