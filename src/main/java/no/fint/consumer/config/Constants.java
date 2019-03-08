
package no.fint.consumer.config;

public enum Constants {
;

    public static final String COMPONENT = "kulturminnevern";
    public static final String COMPONENT_CONSUMER = COMPONENT + " consumer";
    public static final String CACHE_SERVICE = "CACHE_SERVICE";

    
    public static final String CACHE_INITIALDELAY_DISPENSASJONAUTOMATISKFREDAKULTURMINNE = "${fint.consumer.cache.initialDelay.dispensasjonautomatiskfredakulturminne:60000}";
    public static final String CACHE_FIXEDRATE_DISPENSASJONAUTOMATISKFREDAKULTURMINNE = "${fint.consumer.cache.fixedRate.dispensasjonautomatiskfredakulturminne:900000}";
    
    public static final String CACHE_INITIALDELAY_TILSKUDDFARTOY = "${fint.consumer.cache.initialDelay.tilskuddfartoy:70000}";
    public static final String CACHE_FIXEDRATE_TILSKUDDFARTOY = "${fint.consumer.cache.fixedRate.tilskuddfartoy:900000}";
    
    public static final String CACHE_INITIALDELAY_TILSKUDDFREDAHUSPRIVATEIE = "${fint.consumer.cache.initialDelay.tilskuddfredahusprivateie:80000}";
    public static final String CACHE_FIXEDRATE_TILSKUDDFREDAHUSPRIVATEIE = "${fint.consumer.cache.fixedRate.tilskuddfredahusprivateie:900000}";
    

}
