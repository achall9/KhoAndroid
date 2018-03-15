package com.kholabs.khoand.Context;

/**
 * Created by Aladar-Tech on 2017-06-20.
 */

public interface ConstValues {
    public String meta_string_termurl = "https://www.kholabs.com/terms-of-use/";
    public static final int NOTIFICATION_ID = 100;
    public static final String GCM_SENDERID = "9204255472";
    public static final String PARSE_APPLICATION_ID = "feprF30HMaUnBFSqHooMmD5hR8ARSPfpsVTSJUJF";
    public static final String PARSE_SERVER_URL = "https://pg-app-1dj492vrr8pgs8xiceeukfdpyf7wy7.scalabl.cloud/1/";
    public static final String PARSE_CLIENT_KEY = "WOqjzTnX9uSwVy3elc3A9Ss6Pf7SRJTGsFKpSpVs";
    public static final String PARSE_CHANNEL = "KhoAndroid";

    public static boolean DEBUG = false;

    public static class FeedFilter {
        public static final int FeedFilterLatest = 0;
        public static final int FeedFilterNearest = 1;
        public static final int FeedFilterNoAnswers = 2;
        public static final int FeedFilterHighestRated = 3;
        public static final int FeedFilterEmergency = 4;

    };

    public static class FeedType {
        public static final int FeedTypeSupporting = 1;
        public static final int FeedTypeQuestions = 2;
    }

    public static class ProfileFrom {
        public static final int fromFeedPage = 1;
        public static final int fromProfile = 2;
        public static final int fromMessage = 3;
    }

    public static  class PostPageFrom {
        public static final int fromFeedPage = 1;
        public static final int fromSearchFeed = 2;
        public static final int fromProfilePage = 3;
        public static final int fromSupport = 4;
        public static final int fromQuestion = 5;
        public static final int fromComment = 6;
    }
}
