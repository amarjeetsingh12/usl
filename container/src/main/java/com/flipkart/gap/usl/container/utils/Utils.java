package com.flipkart.gap.usl.container.utils;

import com.flipkart.gap.usl.client.utils.ObjectMapperFactory;
import com.flipkart.gap.usl.container.exceptions.InternalException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.flipkart.gap.usl.container.constants.Constants.IST_TIME_ZONE;
import static com.google.common.base.Preconditions.checkArgument;

public class Utils {
    private static final int RANDOM_ID_LENGTH = 10;

    public static synchronized String generateRandomId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, RANDOM_ID_LENGTH);
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty() || str.trim().equalsIgnoreCase("null");
    }

    public static boolean isNullOrEmpty(List list) {
        return list == null || list.isEmpty();
    }

    /**
     * To reflect Monday 31st January 2018, 12:00 PM
     */
    private static final String HUMAN_READABLE_DATE_FORMAT = "%d%s %s %d";

    /**
     * Aimed at returning Monday 31st January 2018, 12:00 PM time string.
     *
     * @param epoch epoch timestamp
     * @return Human readable date string.
     */
    public static String epochToDayMonthString(long epoch) {
        LocalDateTime dateTime = Instant.ofEpochMilli(epoch).atZone(ZoneId.of(IST_TIME_ZONE)).toLocalDateTime();
        return String.format(HUMAN_READABLE_DATE_FORMAT, dateTime.getDayOfMonth(), getDayOfMonthSuffix(dateTime.getDayOfMonth()), dateTime.getMonth().getDisplayName(TextStyle.SHORT, Locale.US), dateTime.getYear());
    }

    private static String getDayOfMonthSuffix(final int n) {
        checkArgument(n >= 1 && n <= 31, "illegal day of month: " + n);
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    /**
     * Helper function to return treeSet of top X elements from a treeSet of elements of type T
     *
     * @param elements
     * @param x
     * @return
     */
    public static <T> TreeSet<T> getTopXElementsFromTreeSet(TreeSet<T> elements, int x) {
        return elements.stream().limit(x).collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Helper function to convert any object to a byte array
     *
     * @param object
     * @param <T>
     * @return
     * @throws InternalException
     */
    public static <T> byte[] getByteArrayFromObject(T object) throws InternalException {
        try {
            return ObjectMapperFactory.getObjectMapper().writeValueAsBytes(object);
        } catch (Throwable t) {
            throw new InternalException("Error converting object to byte array");
        }
    }

    /**
     * used to encode the given string
     * @param url String to be encoded
     * @return returns encoded url
     */
    public static String encode(String url)
    {
        try {
            return URLEncoder.encode( url, "UTF-8" );
        } catch (UnsupportedEncodingException e) {
            return "Issue while encoding" +e.getMessage();
        }
    }

    /**
     * used to decode the given string
     * @param url String to be decoded
     * @return returns decoded url.
     */
    public static String decode(String url)
    {
        try {
            String prevURL="";
            String decodeURL=url;
            while(!prevURL.equals(decodeURL))
            {
                prevURL=decodeURL;
                decodeURL= URLDecoder.decode( decodeURL, "UTF-8" );
            }
            return decodeURL;
        } catch (UnsupportedEncodingException e) {
            return "Issue while decoding" +e.getMessage();
        }
    }

}
