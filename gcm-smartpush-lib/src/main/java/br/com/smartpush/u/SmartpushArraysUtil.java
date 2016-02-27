package br.com.smartpush.u;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by fabio.licks on 02/10/15.
 */
public class SmartpushArraysUtil<T> {

    public String toString( ArrayList<T> list ) {
        if ( list == null ) return "[]";

        StringBuilder sb = new StringBuilder();
        Iterator<T> iterator = list.iterator();

        sb.append( "[" );
        while ( iterator.hasNext() ) {
            T item = iterator.next();
            sb.append( item.toString() );
        }
        sb.append( "]" );

        return sb.toString();
    }
}
