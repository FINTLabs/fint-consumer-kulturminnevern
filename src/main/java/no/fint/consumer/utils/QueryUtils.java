package no.fint.consumer.utils;

public enum QueryUtils {
    ;

    public static String createQuery(String s) {
        return String.format("?%s", s);
    }

}
