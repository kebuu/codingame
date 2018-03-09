import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

public class PlayerIT {

    private String allBoxes = "41.52696 11.71658,86.94442 9.39165,23.88966 21.07889,15.14356 23.38913,23.40995 3.3683,13.32382 23.2853,68.39695 4.87306,47.25742 13.57035,84.74861 12.31977,97.96801 8.21269,51.93992 24.34906,61.86033 8.91908,74.83376 11.57549,47.66093 23.92663,12.94016 5.13788,82.27449 5.923,15.5905 25.46494,62.25963 13.81039,10.63971 10.03906,10.0397 12.71292,51.25867 23.8881,75.49662 18.79441,44.7228 25.19762,97.56636 17.35548,10.87984 16.29957,66.71787 23.03192,61.62631 17.62884,1.75157 14.04116,73.61976 22.66741,53.26511 4.33301,11.07376 17.82265,70.31226 24.13543,12.1525 14.9707,30.51608 4.20517,9.59418 22.88888,35.13426 4.71516,17.3884 9.26509,14.87812 24.28898,52.90244 15.82027,1.83001 25.7073,57.8802 3.52112,76.60104 3.90996,41.42645 1.73196,40.2535 3.55704,35.09492 12.04897,15.37063 6.19589,19.20745 7.61203,1.42646 8.41002,15.34304 15.91841,52.16704 9.17558,49.21093 1.99775,61.08862 10.39981,69.71613 18.30628,15.05507 3.01632,50.89429 24.81932,80.49293 5.86457,76.65203 9.32361,14.35142 19.37968,17.83983 2.64672,67.95807 17.74824,3.87322 5.79793,33.68014 21.68929,84.62015 6.57759,99.39058 21.75325,16.39669 17.79623,43.98981 7.81908,22.33317 24.83129,67.88334 22.28262,20.22455 19.21062,17.93475 11.94931,83.80844 7.24652,86.42124 1.46462,9.8882 7.74076,88.4971 19.49383,21.09327 18.96216,1.66089 2.9609,43.48064 17.05949,6.74683 17.42117,16.69022 23.2145,13.55099 3.06381,66.7617 3.87612,69.93679 19.52882,75.22588 1.42008,16.27799 10.74255,17.67272 15.04395,92.64848 23.36814,49.47578 25.83341,41.96983 18.76231,44.98724 13.7262,97.93178 14.36475,60.91491 8.09562,24.73393 11.687,75.84214 25.71368,54.80682 3.20153,84.19312 1.719,25.24553 15.51654,46.00031 4.64136,79.08826 10.42804,24.70254 16.85265,89.51507 4.88467,41.29874 21.87058,93.34663 15.85575,28.50226 8.06435,40.55836 15.81838,18.59381 5.15226,96.67595 16.19101,74.65276 7.20629,69.1873 7.85082,40.19011 13.83955,5.27411 13.03649,89.09526 3.21732,62.26543 18.11614,11.75345 24.43626,54.49549 22.72689,68.16574 22.20083,59.21605 24.66315,86.81438 21.7187,93.36374 5.87668,90.47042 17.43569,53.64308 3.8006,13.05157 5.18737,48.56048 2.9595,46.92443 25.6673,47.69788 25.44844,70.91372 25.41514,32.12088 25.64722,1.47411 22.36523,1.88191 25.5654,2.9842 12.3169,12.47989 7.86423,93.84292 3.51745,77.49177 18.79353,41.92481 19.97996,15.31155 11.26131,50.94135 14.23222,74.78144 3.23107,79.92943 11.47132,23.10277 19.38496,88.96181 25.25257,3.33218 14.13872,83.55209 3.40644,76.50034 6.797,18.27011 2.70956,3.33673 16.02382,83.55379 11.98724,10.66074 8.25102,11.96375 20.97245,22.76397 7.12055,33.10437 9.65933,93.56035 23.43249,65.39489 16.22322,34.12126 8.03455,29.53299 9.94644,43.68854 25.32656,77.41822 13.1701,4.0449 10.19067,80.48849 6.47781,76.55606 2.80615,47.62262 12.81683,44.7144 20.6173,93.33802 10.89913,16.4442 19.87871,81.8422 19.04469,86.34973 10.32503,75.56031 18.51144,59.31863 23.26596,8.06525 25.52657,41.55705 3.6011,43.1093 13.80419,98.57812 17.23628,97.82532 6.34288,15.04243 24.45644,68.72618 16.897,36.15252 16.72668,3.72295 18.48199,74.21062 24.51075,29.77052 7.42056,22.83998 3.25413,89.70805 9.37255,58.19286 4.12821,46.1814 7.30588,13.66472 23.73502,65.47505 14.43881,45.90061 16.4872,45.63342 8.92723,49.5876 9.25009,72.73321 6.48015,54.37599 10.57598,75.23672 8.04992,93.03039 8.84459,65.614 8.14243,76.46988 13.76593,17.46312 1.86239,15.62675 23.15352,97.30921 21.70162,60.63009 5.86598,63.19233 18.95946,48.26977 7.95658,47.16676 25.52454,23.76942 4.67913,1.27766 23.48601,23.68196 14.27077,49.97663 6.85293,11.64797 7.86604,36.50679 24.59294,9.01001 14.37701,43.06957 22.09615,27.83621 2.78802,97.0515 14.30676,30.02604 5.91704,98.0206 7.71085,44.50913 4.72794,82.55624 5.73998,86.60448 4.48155,9.08193 14.05726,83.00896 14.22167,82.46954 25.34151,36.12727 4.53986,93.44626 19.0459,11.8215 8.34973,4.41684 6.53836,17.06844 13.07235,38.30626 14.21116,56.15096 3.35006," +
            "62.1905 7.39905,93.6619 4.79918,65.75966 22.62618,55.35683 15.09298,92.0301 4.3652,79.432 3.59066,78.38567 7.6198,26.65562 1.61552,32.10536 16.37407,66.28046 9.76935,95.71049 14.09167,83.81615 5.543,65.05023 25.5724,15.33116 14.35687,80.38383 5.43515,22.52706 14.3776,44.53731 4.85128,85.77697 22.54162,24.65111 24.26417,36.99121 7.66544,11.34186 8.19956,14.64987 5.1682,95.61332 21.47771,22.89517 22.6162,99.53263 5.80504,38.52683 18.14345,33.39179 14.43988,63.46074 23.62534,51.91235 2.6632,72.12151 19.88934,44.54067 2.86218,36.65813 19.25706,94.45046 15.07119,94.7693 20.4153,66.54751 15.36177,34.40831 5.84725,18.02684 5.98943,70.64209 1.20119,20.54757 10.38607,40.03273 8.09262,71.50688 18.44317,81.54365 20.3013,30.07208 24.83277,44.18094 14.56091,20.20859 13.5105,54.20428 18.64797,61.5425 2.77067,76.47208 22.46823,84.37408 23.42006,29.27819 11.0906,93.98206 25.16545,72.52346 10.57443,53.06853 1.15292,81.36731 22.44768,41.46779 8.00493,36.98562 15.20043,68.90871 2.18953,65.55709 13.14526,96.02776 22.42435,3.42309 4.33556,49.70458 7.50931,71.08811 21.77641,33.0221 3.05764,41.24459 9.60227,20.95525 7.75372,28.02523 3.8528,75.59144 4.35267,31.3962 13.63632,3.8949 2.54678,65.99431 18.13766,29.18143 12.07777,36.85348 19.91493,78.59037 3.64213,65.54686 9.17473,52.75184 25.36438,86.91992 8.96887,10.93811 2.91422,29.26929 17.07997,43.27361 2.11215,4.29772 14.96718,58.85012 20.59561,39.33126 12.19442,52.1879 15.64237,26.58072 23.30176,3.07683 2.79297,95.81117 2.90521,82.40168 25.49631,51.62914 1.03508,55.14167 10.14253,18.58086 5.41138,48.65391 24.28977,9.37962 3.19258,41.87598 3.57673,70.59018 2.68275,95.7201 15.96977,2.01921 4.85843,50.26356 24.99554,75.35213 17.26821,65.68228 8.89188,16.44446 24.40733,91.4377 12.74723,56.6571 15.8187,15.36607 4.73419,95.75412 5.76716,20.78875 16.45457,21.50736 24.80773,34.82358 13.0797,26.05744 10.8521,65.04842 3.08275,74.4619 13.32926,38.75639 22.90723,56.87377 15.65484,81.15202 11.50725,18.92667 2.7116,40.45275 14.96685,27.83959 22.23793,64.84934 13.612,9.25405 5.44044,40.20724 12.86738,22.07756 15.46931,9.56077 2.01774,17.06773 25.92505,30.9631 9.8827,45.29387 1.51755,14.36641 9.64892,40.78382 2.89083,52.19985 14.68437,20.22587 5.10059,20.0371 6.17261,98.87491 17.63907,75.81785 6.3221,27.46591 18.36509,86.96299 22.4925,47.17646 19.66861,76.92576 8.37009,61.63526 4.70755,53.47759 20.30769,52.30873 10.33367,90.05738 3.64325,31.87605 22.28321,6.60892 8.26553,3.55142 8.86616,11.42459 15.71114,16.93029 21.7768,92.20154 5.39914,79.91903 15.31023,35.00736 1.84406,89.43186 2.2766,14.49534 2.10397,55.9114 14.06675,93.6029 8.3704,35.73554 23.72782,86.00506 7.89475,30.94397 21.9012,15.49515 15.83667,4.78775 18.61714,42.05024 15.3921,79.37141 1.83861,92.28517 5.53134,53.61012 18.43591,63.33442 1.87301,8.65312 10.39516,69.45934 16.34406,73.17588 3.60664,31.57474 17.70807,19.66403 5.38118,36.2954 7.67043,43.79683 19.85865,81.99045 16.20127,61.21773 12.18481,87.978 17.34169,68.85787 12.87197,16.24188 3.71923,15.07649 2.09881,74.27614 2.56802,90.68487 7.79297,98.52639 8.86968,98.30349 3.92574,70.59874 4.64747,90.73057 6.20022,52.82653 5.24015,27.37287 24.4305,95.35061 17.69314,16.00786 2.39395,28.37186 23.50096,84.35774 1.2878,75.49274 14.545,60.62392 7.2222,61.21928 24.90784,97.98752 6.22089,83.67107 7.51919,97.81536 5.51528,8.59933 10.00676,69.64507 13.03417,48.11331 13.31806,61.23862 1.00901,57.2481 17.18723,3.02654 4.37233,74.46298 13.84086,49.4341 15.14105,53.96191 6.4483,45.58526 18.91051,95.87673 18.02984,33.44167 7.37279,87.63537 11.59704,95.64442 9.92244,82.56002 2.50411,1.16563 18.26199,12.85992 6.72351,42.39877 18.04452,94.22901 18.4398,30.64779 14.01892,65.4901 12.99585,48.90289 17.43132,36.10374 11.59199,43.21169 8.99335," +
            "27.1474 4.74525,8.17495 19.01058,69.5713 11.39732,62.81289 16.72835,47.45606 19.01654,2.31272 20.39904,78.8681 18.95626,86.56263 16.66076,46.25013 24.18728,14.81229 19.5558,31.12449 24.14561,75.81931 18.0402,68.46849 3.44379,78.58286 14.15237,88.46341 1.25376,69.78208 5.01311,59.74713 21.08239,24.14601 19.62894,61.93701 11.08865,48.22372 8.4965,92.78323 4.04611,62.09095 2.39729,23.3554 25.37497,11.15271 4.8129,62.97513 8.19903,38.53544 23.36502,56.93178 9.30072,97.79148 14.46302,95.4137 23.72998,1.16125 18.84746,26.56093 4.35418,64.51901 16.45312,14.75938 22.69144,79.7574 2.79083,34.62069 21.34294,47.19416 5.39019,35.29604 17.97833,32.84757 11.67615,38.26661 14.15366,89.50787 17.20266,84.67413 4.0635,2.84468 21.30961,14.31727 1.6291,51.50927 23.51528,8.0 13.11501,58.8105 21.96824,63.96924 24.2311,36.8484 1.60311,65.04223 1.55672,19.88063 21.80268,41.9007 1.07426,79.41802 5.05723,77.88034 13.83861,87.8553 22.88331,61.91043 10.26096,8.31544 21.55031,60.5464 22.24256,24.34989 20.24687,21.28094 17.36535,21.38823 1.47843,12.83483 2.80779,15.61174 9.48851,3.38236 19.04759,67.65488 11.50583,28.19553 6.1624,90.05134 12.45477,22.98562 13.50006,76.66619 21.65428,93.36944 4.32446,32.74387 2.93221,29.59991 6.50371,80.61269 18.4584,11.1792 1.51936,51.63201 6.67018,96.52921 8.72443,54.19727 21.63571,18.42467 20.36587,27.96584 2.00918,5.27432 7.73642,50.84757 4.01445,56.72733 21.60026,92.05632 2.31381,78.52327 4.11231,5.78864 10.75638,38.9264 16.04347,13.71071 24.42634,3.14909 3.24994,48.87206 2.59775,91.9444 21.73057,11.56283 8.24549,56.92826 9.04211,44.4051 23.64145,35.91863 15.10527,54.26634 20.8644,85.65042 22.33207,95.97648 17.09482,29.56689 12.68419,2.12835 10.79286,7.49751 16.49599,25.70304 11.25561,34.43868 23.3864,8.0194 2.27464,48.96619 17.6058,51.98572 17.78403,88.90404 11.62712,58.65514 25.98116,86.96788 20.46454,8.24431 24.80413,20.86311 12.66733,32.77002 18.79871,18.14761 18.32552,4.11285 8.44575,74.45621 7.56493,90.229 10.14751,94.61059 20.03579,86.81365 1.16724,74.22242 18.26929,97.85312 17.42464,80.64114 24.98205,5.43948 3.21767,32.89669 8.05284,82.45971 8.21726,60.38691 24.04831,9.48053 4.17917,47.84457 25.82194,85.16464 24.16822,44.85606 2.43533,84.36995 7.35985,62.04942 10.19077,81.8862 23.98388,16.94992 3.43784,22.8307 15.15483,91.48328 19.81834,21.71134 9.68014,34.41234 22.69125,6.73816 13.4217,47.06849 6.40322,5.90939 13.57594,50.39824 18.98368,99.27816 9.13935,59.05282 9.10639,59.79734 6.04479,55.99369 6.09361,55.33028 9.53621,88.335 5.81001,1.31002 19.07192,98.3551 25.48786,45.58006 24.23658,20.36244 5.38775,37.72594 16.76964,4.40522 19.08701,5.42343 11.79861,5.68992 4.41998,32.18149 13.12018,19.45627 9.92256,17.69985 2.25538,61.4441 22.1203,64.07345 15.03652,33.31442 6.58987,77.59447 15.55318," +
            "43.03858 21.02288,39.69534 2.84599,87.98408 15.85319,53.73791 10.21775,98.38259 13.07385,51.51255 25.26042,86.47121 11.56351,22.46708 18.73673,67.48083 7.52943,68.43604 11.16325,67.58315 11.89637,65.88055 13.15663,90.50811 25.74578,26.00374 22.40529,3.19936 7.42616,58.8539 15.50646,82.92402 23.55424,28.9714 12.67978,61.35995 17.70026,32.09732 10.36205,88.51374 1.77707,85.32974 23.82783,77.04071 7.32005,13.20132 12.39557,59.36396 7.77436,48.61437 6.89606,95.90168 18.51917,91.16312 11.21092,30.30977 14.77825,14.83632 18.54723,84.28061 6.17527,37.47005 12.62901,26.70622 8.65831,82.58659 3.12539,54.48754 16.71659,20.37769 7.91533,63.79497 6.13119,39.44498 1.6219,33.84917 16.56145,85.24641 25.70022,96.27914 3.18332,85.43401 8.24213,80.44197 19.19096,66.30993 11.2605,23.39578 4.33028,83.83772 16.95085,2.18568 7.23877,23.68515 25.07732,29.64277 25.42836,32.346 6.61495,25.14269 12.18964,55.03712 23.39115,41.29979 21.49693,17.61765 2.82852,96.40544 20.0522,47.01078 19.52064,80.74283 15.11731,37.90134 4.50105,20.39709 7.52481,57.70228 6.04537,61.09367 10.82425,11.95129 20.38354,30.58795 10.29373,45.8715 16.02291,2.96808 21.30849,37.18573 10.2185,53.01408 15.47976,55.94926 8.97257,8.70524 11.05122,45.3336 23.98493,22.62845 3.00701,37.37611 5.90187,52.48968 21.36787,9.92958 20.17982,63.14237 4.6324,11.36573 16.24941,93.55641 24.73886,80.76102 17.24436,72.13557 24.33344,10.1036 2.92226,17.10333 17.12436,46.21494 19.66591,4.91925 19.84506,7.13995 21.53949,30.012 9.6257,60.50884 5.44543,57.58724 2.09117,93.0965 4.70907,77.08283 12.46838,47.52181 2.91067,86.1392 5.77194,22.79972 7.23803,56.73495 20.83906,47.42535 2.21084,48.89608 8.59693,72.59366 23.69437";

    @Test
    public void test() {
        Player.Params params = new Player.Params();

        params.boxes = getBoxes(200);//.stream().sorted((b1, b2) -> Double.compare(b2.volume, b1.volume)).collect(Collectors.toList());
        params.populationSize = 10000;
        params.bestSolutionSelectionCount = 50;
        params.executionMaxTime = 100000;
        //params.executionMaxIteration = 10;
        params.invalidSolutionPenalty = 100.;
        params.debug = false;

        System.out.println(params.boxes.stream().map(box -> String.valueOf(box.weight)).collect(Collectors.joining("|")));

        int[] solution = Player.play(params);
        Player.print(solution);
    }

    List<Player.Box> getBoxes() {
        List<Player.Box> boxes = new ArrayList<>();
        String[] splitBoxes = allBoxes.split(",");

        for (int i =0 ; i < splitBoxes.length; i++) {
            String[] splitWeightVolume = splitBoxes[i].split(" ");
            boxes.add(new Player.Box(i, Double.valueOf(splitWeightVolume[0]),Double.valueOf(splitWeightVolume[1])));
        }

        return boxes;
    }

    List<Player.Box> getBoxes(int size) {
        return getBoxes().subList(0, size);
    }
}