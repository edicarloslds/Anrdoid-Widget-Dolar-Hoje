package br.com.power.dolarhoje;

import java.util.HashMap;

/**
 * Criado por Edicarlos em 11/03/2016 22:39.
 */
public class GetCode {

    static HashMap<String, Integer> codeHash = new HashMap<>();

    static {
        init();
    }

    public static void init() {
        codeHash.put("Dólar Americano (venda)",1);
        codeHash.put("Dólar Americano (compra)",10813);
        codeHash.put("Euro (venda)",21619);
        codeHash.put("Euro (compra)",21620);
        codeHash.put("Iene (venda)",21621);
        codeHash.put("Iene (compra)",21622);
        codeHash.put("Libra Esterlina (venda)",21623);
        codeHash.put("Libra Esterlina (compra)",21624);
        codeHash.put("Franco Suíço (venda)",21625);
        codeHash.put("Franco Suíço (compra)",21626);
        codeHash.put("Coroa Dinamarquesa (venda)",21627);
        codeHash.put("Coroa Dinamarquesa (compra)",21628);
        codeHash.put("Coroa Norueguesa (venda)",21629);
        codeHash.put("Coroa Norueguesa (compra)",21630);
        codeHash.put("Coroa Sueca (venda)",21631);
        codeHash.put("Coroa Sueca (compra)",21632);
        codeHash.put("Dólar Australiano (venda)",21633);
        codeHash.put("Dólar Australiano (compra)",21634);
        codeHash.put("Dólar Canadense (venda)",21635);
        codeHash.put("Dólar Canadense (compra)",21636);
    }


    public static Integer getCode(String param) {

        if(codeHash.get(param) != null){
            return codeHash.get(param);
        }else {
            return 0;
        }
    }

}
