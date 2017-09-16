package pl.edu.pw.mmajews2.photophoto.decoding;

import java.util.ArrayList;

/**
 * Created by Maciej Majewski on 2016-05-15.
 */
public class RoundRobinList<E> extends ArrayList<E> {
    private int current = 0;

    @Override
    public E get(int index) {
        if(index < 0)
            index = size() - Math.abs(index) % size();
        return super.get(index % size());
    }

    public E getNext(){
        return get(++current);
    }

    public E getPrevious(){
        return get(--current);
    }

    public E getCurrent(){
        return get(current);
    }
}
