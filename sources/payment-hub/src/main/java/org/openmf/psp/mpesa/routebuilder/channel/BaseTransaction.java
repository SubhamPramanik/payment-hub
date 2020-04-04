package org.openmf.psp.mpesa.routebuilder.channel;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.openmf.psp.mpesa.config.MPesaSettings;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BaseTransaction extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("direct:conductTransaction")
                .id("conductTransaction")
                .log("Starting transaction")
                .to("direct:getAccessToken")
        ;

        from("direct:getAccessToken")
                .id("getAccessToken")
                .log("Initiated Process to get Access Token")
                .process("fetchAccessTokenProcessor")
                .choice()
                .when(exchange -> exchange.getProperty("tokenResponseCode", String.class).equals("200"))
                .log("Access Token fetch successful, moving to transaction.")
                .to("direct:commitTransaction")
                .otherwise()
                .log("Access token fetch unsuccessful.")
                .to("direct:transactionFailure")
        ;

        from("direct:commitTransaction")
                .id("commitTransaction")
                .log("Committing Transaction")
                .process("postTransactionProcess")
                .choice()
                .when(exchange -> exchange.getProperty("transactionResponseCode", String.class).equals("200"))
                .log("Transaction was successful")
                .to("direct:endTransaction")
                .otherwise()
                .log("Transaction failed!")
        ;

        from("direct:endTransaction")
                .id("endTransaction")
                .choice()
                .when(exchange -> exchange.getProperty("transactionType", String.class).equals(MPesaSettings.MPesaBinding.BALANCE))
                .marshal().json(JsonLibrary.Jackson)
        ;

    }

}
