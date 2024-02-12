package com.plmtmobile.scala40se;

import java.util.Comparator;

/**
 * Created by daniele on 23/10/13.
 */
public class CandidatesComparator implements Comparator<EvaluationResult> {
    @Override
    public int compare(EvaluationResult o1, EvaluationResult o2) {
        if( o1.score > o2.score )
            return -1;
        else if ( o1.score < o2.score )
            return 1;
        else
            return 0;
    }
}
