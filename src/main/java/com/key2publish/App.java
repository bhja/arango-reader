package com.key2publish;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.key2publish.model.ProgramParams;
import com.key2publish.service.DataService;
import java.io.File;

/**
 *
 *
 */
public class App 
{

    public static void main(String... args )
    {
        ObjectMapper mapper  = new ObjectMapper();
        try {
            if(args.length==0){
                System.err.println("Need the file with json format as argument");
                System.exit(1);
            }
            ProgramParams params = mapper.readValue(new File(args[0]), ProgramParams.class);
            DataService service = new DataService();
            if(params.action().equals("import")){
                service.importCsv(params);
            }else{
                service.httpCall(params,"FOR c IN k2p_product RETURN {key:c._key , code:c.code}");
            }


        }catch (Exception e){
            System.err.println("Could not read the file due to " + e.getMessage());
            System.exit(1);
        }
    }
}
