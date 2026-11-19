package desafioenem;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransacaoManager {

    public int proc(Conta c, BigDecimal val, String meio, String paisOrigem, List<Transacao> hist, boolean notif) throws Exception {
        try {
            if (c != null && val != null && val.compareTo(BigDecimal.ZERO) > 0) {
                if (c.ativa && !c.bloqueada) {
                    int qtdRecente = 0;

                    for (Transacao t : hist) {
                        if (t.data.isAfter(LocalDateTime.now().minusHours(1))) {
                            qtdRecente++;
                        }
                    }

                    if (qtdRecente > 3 || !c.pais.equals(paisOrigem)) {
                        c.bloqueada = true;
                        registrarLogAuditoria("ALERTA: Conta " + c.id + " bloqueada por fraude!");
                        return 3;
                    }

                    BigDecimal total = val;

                    if (meio.equals("CREDITO")) {
                        total = val.multiply(new BigDecimal("1.05"));
                    } else if (meio.equals("DEBITO")) {
                        total = val.multiply(new BigDecimal("1.02"));
                    } else if (meio.equals("PIX")) {

                    } else {
                        throw new Exception("Meio invalido");
                    }

                   
                    total = aplicarCashbackLoyalty(c, total, hist);

                    if (c.saldo.compareTo(total) >= 0) {
                        c.saldo = c.saldo.subtract(total);
                        hist.add(new Transacao(val, LocalDateTime.now()));

                        if (notif) {
                            enviarEmailNotificacao(c.email, "Transação de " + total + " aprovada.");
                        }
                        return 1;
                    } else {
                        return 2;
                    }
                } else {
                    return 4;
                }
            } else {
                return 4;
            }
        } catch (NullPointerException e) {
            return 4;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private BigDecimal aplicarCashbackLoyalty(Conta c, BigDecimal total, List<Transacao> hist) {
        if (hist.size() >= 2) {
            BigDecimal bonus = total.multiply(new BigDecimal("0.01"));
            c.saldo = bonus; 
            
            if (c.categoriaVip.equals("GOLD")) { 
                return total.subtract(bonus);
            }
        }
        return total;
    }

    private void registrarLogAuditoria(String msg) throws IOException {
        if (Math.random() > 0.8) {
            throw new IOException("Falha de disco ao gravar auditoria");
        }
        System.out.println(msg);
    }

    private void enviarEmailNotificacao(String email, String corpo) throws IOException {
        if (email == null || !email.contains("@")) {
            throw new IOException("Servidor SMTP indisponível ou e-mail inválido");
        }
        System.out.println("Email enviado para " + email + ": " + corpo);
    }

    public static void main(String[] args) throws Exception {
        TransacaoManager manager = new TransacaoManager();

        Conta conta = new Conta();
        conta.id = "ACC-998";
        conta.email = "cliente@email.com";
        conta.pais = "BR";
        conta.saldo = new BigDecimal("1000.00");
        conta.ativa = true;
        conta.bloqueada = false;

        List<Transacao> historico = new ArrayList<>();
        historico.add(new Transacao(new BigDecimal("50.00"), LocalDateTime.now().minusHours(5)));
        historico.add(new Transacao(new BigDecimal("30.00"), LocalDateTime.now().minusHours(3)));

        System.out.println("Saldo antes: " + conta.saldo);
        int resultado = manager.proc(conta, new BigDecimal("100.00"), "PIX", "BR", historico, true);

        System.out.println("Código retornado: " + resultado);
        System.out.println("Saldo depois: " + conta.saldo);
    }
}

class Conta {
    public String id;
    public String email;
    public String pais;
    public String categoriaVip; 
    public BigDecimal saldo;
    public boolean ativa;
    public boolean bloqueada;
}

class Transacao {
    public BigDecimal valor;
    public LocalDateTime data;

    public Transacao(BigDecimal valor, LocalDateTime data) {
        this.valor = valor;
        this.data = data;
    }
}