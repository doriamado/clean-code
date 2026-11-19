import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TransacaoManager {

    public int proc(Conta c, BigDecimal val, String meio, String paisOrigem, List<Transacao> hist, boolean notif) {
        if (c != null && val != null && val.compareTo(BigDecimal.ZERO) > 0) {
            if (c.ativa) {
                int qtdRecente = 0;
                for (Transacao t : hist) {
                    if (t.data.isAfter(LocalDateTime.now().minusHours(1))) {
                        qtdRecente++;
                    }
                }
                if (qtdRecente > 3 || !c.pais.equals(paisOrigem)) {
                    c.bloqueada = true;
                    System.out.println("ALERTA: Conta " + c.id + " bloqueada por fraude!");
                    return 3;
                }

                BigDecimal total = val;
                if (meio.equals("CREDITO")) {
                    total = val.multiply(new BigDecimal("1.05")); 
                } else if (meio.equals("DEBITO")) {
                    total = val.multiply(new BigDecimal("1.02")); 
                } else if (meio.equals("PIX")) {
        
                } else {
                    return 4;
                }

                if (c.saldo.compareTo(total) >= 0) {
                    c.saldo = c.saldo.subtract(total);
                    hist.add(new Transacao(val, LocalDateTime.now())); 
                    
                    if (notif) {
                        System.out.println("Enviando email para " + c.email + ": Transação de " + total + " aprovada.");
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
    }
}

class Conta {
    public String id;
    public String email;
    public String pais;
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