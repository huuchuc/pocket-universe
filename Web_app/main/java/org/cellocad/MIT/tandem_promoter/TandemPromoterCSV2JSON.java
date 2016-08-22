package org.cellocad.MIT.tandem_promoter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.cellocad.MIT.dnacompiler.Args;
import org.cellocad.MIT.dnacompiler.Util;
import org.json.simple.JSONObject;

import java.util.ArrayList;



public class TandemPromoterCSV2JSON {

    public static JSONObject all_fns;

    public static void main(String[] args) {

        Args options = new Args();

        String path = options.get_home() + "/resources/data/tandem_promoters/";

        String in_csv  = path + "tandem_promoter_params_111015.csv";

        String out_json = path + "tandem_promoter_params_111015.json";

        ArrayList<ArrayList<String>> in_tokens = Util.fileTokenizer(in_csv);


        /**
         * 4-input tandem promoter data trumps 2-input tandem promoter data.
         */

        all_fns = new JSONObject();
        tokens2json(in_tokens, "in111015"); //will overwrite



        /**
         * Write a single json file for tandem promoter data from both 4-input and 2-input backbones.
         */
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        String jsontext = gson.toJson(all_fns);
        Util.fileWriter(out_json, jsontext, false);



    }


    public static void tokens2json(ArrayList<ArrayList<String>> tokens_list, String source) {

        String hill_activation = "ymin+(ymax-ymin)/(1.0+(K/x)^n)";
        String hill_repression = "ymin+(ymax-ymin)/(1.0+(x/K)^n)";


        for(ArrayList<String> tokens: tokens_list) {

            if(tokens.size() <= 3) {
                continue;
            }


            String gateA = tokens.get(0);
            String gateB = tokens.get(1);
            String state = tokens.get(2);
            Double ymax = Double.valueOf(tokens.get(3));
            Double ymin = Double.valueOf(tokens.get(4));
            Double K = Double.valueOf(tokens.get(5));
            Double n = Double.valueOf(tokens.get(6));

            String tandem_promoter_name = gateA + "_" + gateB;
            String equation_name = state + "_equation";
            String params_name = state + "_params";


            if(! all_fns.containsKey(tandem_promoter_name)) {
                all_fns.put(tandem_promoter_name, new JSONObject());
            }


            JSONObject tp_obj = (JSONObject) all_fns.get(tandem_promoter_name);
            tp_obj.put("collection", "tandem_promoter");
            tp_obj.put("gateA", gateA);
            tp_obj.put("gateB", gateB);
            tp_obj.put("source", source);


            JSONObject params = new JSONObject();




            String gate_name = "";

            if(equation_name.startsWith("0x") || equation_name.startsWith("1x")) {
                gate_name = gateB;
            }
            else {
                gate_name = gateA;
            }

            String type = "";

            if(gate_name.contains("pTac") || gate_name.contains("pTet") || gate_name.contains("pBAD") || gate_name.contains("pLuxStar")) {
                type = "INPUT";
            }
            else {
                type = "NOT";
            }

            String regulation = "";

            if(type.equals("INPUT")) {
                regulation = "activation";
            }
            else if(type.equals("NOT")){
                regulation = "repression";
            }


            params.put("ymax", ymax);
            params.put("ymin", ymin);
            params.put("K", K);
            params.put("n", n);
            tp_obj.put(params_name, params);


            if(regulation.equals("activation")) {
                tp_obj.put(equation_name, hill_activation);
            }
            else if(regulation.equals("repression")) {
                tp_obj.put(equation_name, hill_repression);
            }

        }


    }

}
