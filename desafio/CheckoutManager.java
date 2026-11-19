package desafio;

import java.math.BigDecimal;

public class CheckoutManager {

    public int fecharCompra(Cliente c, BigDecimal valorCompra, String cupom) {
        try {
            if (c != null && valorCompra != null && valorCompra.compareTo(BigDecimal.ZERO) > 0) {
                if (c.ativo) {
                    BigDecimal total = valorCompra;

                    if (cupom != null) {
                        if (cupom.equals("DESC10")) {
                            total = valorCompra.multiply(new BigDecimal("0.90"));
                        } else if (cupom.equals("DESC20")) {
                            total = valorCompra.multiply(new BigDecimal("0.80"));
                        } else {
                            return 3;
                        }
                    }

                    total = aplicarBeneficioFidelidade(c, total);

                    if (c.saldo.compareTo(total) >= 0) {
                        c.saldo = c.saldo.subtract(total);
                        System.out.println("Compra aprovada para " + c.nome + ". Total pago: " + total);
                        return 1;
                    } else {
                        return 2;
                    }
                } else {
                    return 3;
                }
            } else {
                return 3;
            }
        } catch (NullPointerException e) {
            return 3;
        }
    }

    private BigDecimal aplicarBeneficioFidelidade(Cliente c, BigDecimal total) {
        if (total.compareTo(new BigDecimal("100.00")) >= 0) {
            BigDecimal cashback = total.multiply(new BigDecimal("0.05"));
            c.saldo = cashback; 

            if (c.nivelFidelidade.equals("OURO")) {
                return total.subtract(cashback);
            }
        }
        return total;
    }

    public static void main(String[] args) {
        CheckoutManager checkout = new CheckoutManager();

        Cliente cliente = new Cliente();
        cliente.nome = "Ana Silva";
        cliente.saldo = new BigDecimal("500.00");
        cliente.ativo = true;

        System.out.println("Saldo antes: " + cliente.saldo);
        int status = checkout.fecharCompra(cliente, new BigDecimal("200.00"), "DESC10");

        System.out.println("Status retornado: " + status);
        System.out.println("Saldo depois: " + cliente.saldo);
    }
}

class Cliente {
    public String nome;
    public String nivelFidelidade;
    public BigDecimal saldo;
    public boolean ativo;
}
