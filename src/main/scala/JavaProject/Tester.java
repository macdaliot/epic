package JavaProject;

import java.util.*;
import java.io.*;
import java.lang.*;
import java.nio.channels.FileChannel;
import epic.sequences.SemiCRF;
import epic.sequences.SemiConllNerPipeline;
import org.apache.commons.lang3.math.NumberUtils;

public class Tester {
    public static int batchSize = 10;
    public static String methodChoice = "LC";
    public static double noise = 0;//0.15/(4000/274);
    public static boolean boo =true;
    public static List<String[]> trainingStrings = new ArrayList<>();
    public static String[] trainingString = {"--train",
            "data/labeledPool.conll",
            "--test", "data/conllFileTest.conll",
            "--modelOut", "data/our_malware.ser.gz","--useStochastic","false","--regularization","1"};
    public static boolean noiseCut = false;
    public static boolean error = false;
    public static double threshold = 0;
    public static boolean quadraticBatchSize = false;
    public static int originalBatchSize = 0;
    public static boolean infodens = false;
    public static boolean random = false;
    public static int labeledPoolSize = 0;
    public static List<SemiCRF<String, String>> models = new ArrayList<>();
    public static File fileNameUnlabeledSet;
    public static File fileNameLabeledSet;
    public static String modelFileName = "./data/our_malware.ser.gz";
    public static int totalPoolSize = 0;
    public static String user = "";

    public static void main(String[] args) {
        // First input is user name
        // second input is batch size
        // Input "train" to start the run of with training
        // Input "stochastic" to use the stochastic epic model
        // Input "gibbs"/"lc" to choose active learning method
        // Input "noise" to add batch cutting
        // Input "error" for error adjustment
        // Input "db" to retrain the database

        //************* SETUP *************
        user = args[0];
        copyFile(); //Copys sets to txt files
        // Filenames
        fileNameUnlabeledSet = new File("/Users/" + args[0] + "/epic/epic/data/unlabeledPool.txt");
        fileNameLabeledSet = new File("/Users/" + args[0] + "/epic/epic/data/labeledPool.txt");
        totalPoolSize = getPoolSize(fileNameLabeledSet, fileNameUnlabeledSet);

        setStaticVariables(args);

        System.out.println("Welcome " + args[0]);
        long startTime = System.currentTimeMillis();
        double noiseParameter = 1;
        String s = null;
        List<List<Double>> informationDensities = getInfoDens(infodens);
        System.out.println("InfoDens is of size: "+ informationDensities.size());

        // Initialize objects
        SelectQuery sq = new SelectQuery();
        SelectQueryRandom sqr = new SelectQueryRandom();
        CreatePythonFile cp = new CreatePythonFile();
        Batch b;
        PrintWriter writer;

        try {
            writer = new PrintWriter("/Users/" + args[0] + "/epic/epic/data/unsure.txt", "UTF-8");

            List<Double> batch = new ArrayList<Double>();
            batch.add(0.0);
            int c = 0;
            PrintWriter pw = new PrintWriter("data/stats.txt");
            try {
                pw.write("Training stats:\n");
                pw.close();
                pw = new PrintWriter("data/labeledRunSize.txt");
                pw.write("Labeled pool size:\n");
                pw.close();
            } catch(IOException fe){ System.out.println("Error in opening print writers: "+ fe);}


            boolean labelNewBatch = true;

            // *********** SETUP DONE **************
            // *********** START RUNS **************
            while(boo ) {
                if (labelNewBatch) {
                    c++;
                    if(quadraticBatchSize) {
                        batchSize =  c*c* originalBatchSize;
                        if (batchSize>10000){batchSize = 10000;}

                    }
                    System.out.println("Batchsize: "+batchSize);
                    System.out.println("******** Batch number " + c + " evaluating **********\n");

                    //********** NOISE ADAPTION *************
                    if (noiseCut) { //Noise adjustment -> don't pick the hardest
                        batch =  getBatchNoiseCut(noiseParameter,sq, informationDensities);
                        addLabeledSizeToFile(pw);
                    } else{
                        System.out.println("Batch is of length (before)" + batchSize);
                        //********** RANDOM *************
                        if(random){ b = sqr.SelectQueryRandom(fileNameUnlabeledSet, batchSize);}
                        //********** NO ADD-ONS *************
                        else {
                            b = sq.SelectQuery(fileNameUnlabeledSet, batchSize, methodChoice, models, threshold, informationDensities);
                        }
                        batch = b.getIds();
                        labeledPoolSize += batch.size();
                        System.out.println("Labeled pool size: "+labeledPoolSize);
                        addLabeledSizeToFile(pw);
                        System.out.println("Batch is of length (no noise)" + batch.size());
                    }

                    //*********** ADD CHOSEN BATCH AND RETRAIN ***********
                    if (batch.size() == 0) {
                        break;
                    }
                    moveBatch(cp,noise,batch,labelNewBatch);
                    Train(trainingStrings);
                    if (c == 1000){
                        labelNewBatch = false;
                    }
                }

                //************ RELABEL ************
                else if (!labelNewBatch & error) { //"Relabel"
                    System.out.println("************RELABELING********\n" +
                            "              ******\n             **********\n");
                    SemiCRF<String, String> model = getModel.getModel(modelFileName);
                    b = sq.SelectQuery(fileNameLabeledSet, 200, methodChoice, models,threshold,informationDensities);
                    writeUnsure(b,batchSize, writer);

                    if (batch.size() == 0) {
                    break;
                    }

                    moveBatch(cp,noise,batch,labelNewBatch);

                    Train(trainingStrings);

                    labelNewBatch = true;
                }

            }
            long endTime = System.currentTimeMillis();

            System.out.println("That took " + (endTime - startTime) + " milliseconds");
        } catch (IOException u) {
            System.out.println("Unsure couldn't open " +u);
        }
    }

    private static void addLabeledSizeToFile(PrintWriter pw){
        try{
            pw = new PrintWriter(new FileOutputStream(
                    new File("data/labeledRunSize.txt"),true));
            pw.append(labeledPoolSize+"\n");
            pw.close();
        } catch (IOException f) {
            System.out.println("AddlabeledSizeToFile error: "+f);
        }
    }

    private static List<Double> getBatchNoiseCut(double noiseParameter,SelectQuery sq, List<List<Double>> informationDensities){
        double sizeOfLabeledPool = sizeOfFile(fileNameLabeledSet);
        double sizeOfUnlabeledPool = sizeOfFile(fileNameUnlabeledSet);
        System.out.println("Total pool: "+ totalPoolSize+ "  labeled pool: "+sizeOfLabeledPool+ " size unlabeled: "+ sizeOfUnlabeledPool);
        int amountToCut = (int) ((sizeOfLabeledPool)*
                (sizeOfLabeledPool * noiseParameter/totalPoolSize));
        System.out.println("Cuttin away "+amountToCut);
        Batch b = sq.SelectQuery(fileNameUnlabeledSet, batchSize+amountToCut, methodChoice, models,threshold, informationDensities);
        List<Double> batch = b.sortBatch();
        System.out.println("Batch before cut is of length (noise)" + batch.size());
        if (batch.size()>batchSize) {
            batch = batch.subList(0, batchSize);
        }
        labeledPoolSize += batch.size();
        System.out.println("Batch is of length (noise)" + batch.size());
        System.out.println("Labeled pool size: "+labeledPoolSize);
        return batch;
    }
    private static void copyFile(){
        File sourceFile1 = new File("/Users/" + user + "/epic/epic/data/unlabeledPoolStart.txt");
        File destFile1 = new File("/Users/" + user + "/epic/epic/data/unlabeledPool.txt");
        File sourceFile2 = new File("/Users/" + user + "/epic/epic/data/labeledPoolStart.txt");
        File destFile2 = new File("/Users/" + user + "/epic/epic/data/labeledPool.txt");
        File sourceFile3 = new File("/Users/" + user + "/epic/epic/data/labeledPoolStart.conll");
        File destFile3 = new File("/Users/" + user + "/epic/epic/data/labeledPool.conll");

        FileChannel source1 = null;
        FileChannel destination1 = null;
        FileChannel source2 = null;
        FileChannel destination2 = null;
        FileChannel source3 = null;
        FileChannel destination3 = null;
        try{
            try {
                source1 = new FileInputStream(sourceFile1).getChannel();
                destination1 = new FileOutputStream(destFile1).getChannel();
                destination1.transferFrom(source1, 0, source1.size());
                source2 = new FileInputStream(sourceFile2).getChannel();
                destination2 = new FileOutputStream(destFile2).getChannel();
                destination2.transferFrom(source2, 0, source2.size());
                source3 = new FileInputStream(sourceFile3).getChannel();
                destination3 = new FileOutputStream(destFile3).getChannel();
                destination3.transferFrom(source3, 0, source3.size());
            }
            finally {
                if(source1 != null) {
                    source1.close();
                }
                if(destination1 != null) {
                    destination1.close();
                }
                if(source2 != null) {
                    source2.close();
                }
                if(destination2 != null) {
                    destination2.close();
                }
                if(source3 != null) {
                    source3.close();
                }
                if(destination3 != null) {
                    destination3.close();
                }
            }
        }
        catch (IOException ex) {
            System.out.println(
                    "Something went wrong when getRunTime on first training: " + ex);
        }


    }


    private static double sizeOfFile(File fileName){
        System.out.println("******** Create all pools and datasets **********\n");
        String s = null;
        double size = 0.0;
        try {
            FileReader tmpR = new FileReader(fileName);
            BufferedReader tmp = new BufferedReader(tmpR);
            while ((tmp.readLine()) != null) {
                size++;
            }
        } catch (IOException ex) {
            System.out.println(
                    "Something went wrong when getRunTime on first training: " + ex);
        }
        return size;

    }

    private static void splitAndWriteDB(double noise){
        System.out.println("******** Create all pools and datasets **********\n");
        String s = null;
        try {
            Process p = Runtime.getRuntime().exec("python src/main/scala/JavaProject/PythonScripts/writeFilesFromDatabase.py 0.8 1"+Double.toString(noise));
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));
            // read the output from the command
            System.out.println("Here is the standard output of the command writeFiles:\n");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }
        } catch (IOException ex) {
            System.out.println(
                    "Something went wrong when getRunTime on first training: " + ex);
        }

    }

    private static int getPoolSize(File fileNameLabeledSet, File fileNameUnlabeledSet){
        int size = 0;
        try{
            FileReader tmpL = new FileReader(fileNameLabeledSet);
            FileReader tmpUn = new FileReader(fileNameUnlabeledSet);
            BufferedReader tmpl = new BufferedReader(tmpL);
            BufferedReader tmpun = new BufferedReader(tmpUn);
            while ((tmpl.readLine()) != null) {
                size++;
            }
            labeledPoolSize = size;
            while ((tmpun.readLine()) != null) {
                size++;
            }
            System.out.println("Total pool size is "+ size);
        } catch (IOException ex) {
            System.out.println(
                    "Something went wrong when getRunTime on first training: " + ex);
        }
        return size;

    }

    private  static void Train(List<String[]> trainingStrings) {
        if (trainingStrings.size() > 1) { // If vote, split the labeled conll before training.
            System.out.println("******** Splitting child conll **********\n");
            String s = null;
            deleteDirectory(new File("/Users/" + user + "/epic/epic/data/unlabeledPool.txt"));
            try {
                Process p = Runtime.getRuntime().exec("python src/main/scala/JavaProject/PythonScripts/makeChildConll.py "
                        + labeledPoolSize + " " + (trainingStrings.size() - 1)); // Input number of lines and number of models
                BufferedReader stdInput = new BufferedReader(new
                        InputStreamReader(p.getInputStream()));
                System.out.println("Here is the standard output of the command writeFiles:\n");
                while ((s = stdInput.readLine()) != null) {
                    System.out.println(s);
                }
            } catch (IOException ex) {
                System.out.println(
                        "Something went wrong when getRunTime on first training: " + ex);
            }
        }
        System.out.println("TrainingString: before training \"" + Arrays.deepToString(trainingStrings.toArray())+"\"");
        for (int i = 0; i < trainingStrings.size(); i++){
            if (i == 0) {
                System.out.println("Training main model");
            } else {
                System.out.println("Training child model " + i );
            }
        SemiConllNerPipeline.main(trainingStrings.get(i));
        }
    }

    public static void deleteDirectory(File directory) {
        if(directory.exists()){
            File[] files = directory.listFiles();
            if(null!=files){
                for(int i=0; i<files.length; i++) {
                    if(files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    }
                    else {
                        files[i].delete();
                    }
                }
            }
        }
    }

    private static void moveBatch(CreatePythonFile cp, double noise,List<Double> batch, Boolean newBatch){
        System.out.println("******** Move a batch **********");
        cp.CreatePythonFile(batch, noise, newBatch);
        String s=null;
        try {
            Process p = Runtime.getRuntime().exec("python src/main/scala/JavaProject/PythonScripts/tmp.py");
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));
            // read the output from the command
            System.out.println("Here is the standard output of the command MoveBatch:\n");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }
        } catch (IOException ex) {
            System.out.println(
                    "Something went wrong when getRunTime of tmp");
        }

    }

    private static void writeUnsure(Batch b, int batchSize,PrintWriter writer){

        List<Double>batch = b.getIds();
        List<String>sentences = b.getSentences();
        int medSentLength = 0;
        String medLC = "";
        for (int i = 0; i < batch.size(); i++) {
            String tmp = sentences.get(i).replace(". . \n","");
            tmp = tmp.replace(". . B_MALWARE\n","");
            tmp = tmp.replace(". . I_MALWARE\n","");
            //System.out.println(tmp);
            String[] splitSentence = tmp.split(" ");
            medSentLength += (splitSentence.length-1)/batchSize;
            medLC += Double.parseDouble(splitSentence[0])/batchSize;
            writer.println(batch.get(i) + " " +sentences.get(i));
        }
        writer.println("Medium LC value: " + medLC+" Medium sent length: " + medSentLength);
    }

    private static List<List<Double>> getInfoDens(boolean id){
        if(id) {
            List<List<Double>> infoDens = new ArrayList<>();
            List<Double> ids = new ArrayList<>();
            List<Double> densities = new ArrayList<>();
            File infoFile = new File("/Users/" + user + "/epic/epic/data/simTestFile.txt");
            try{
                FileReader tmp = new FileReader(infoFile);
                BufferedReader tmpb = new BufferedReader(tmp);
                String s = null;
                String[] split;

                while ((s=tmpb.readLine()) != null) {
                    split = s.split(" ");
                    ids.add(Double.parseDouble(split[0]));
                    densities.add(Double.parseDouble(split[1]));
                }
                infoDens.add(ids);
                infoDens.add(densities);
                System.out.println("InfoDens is of size: "+ infoDens.size() +" and length "+ infoDens.get(0).size());
                return infoDens;
            } catch (IOException ex) {
                System.out.println(
                        "Something went wrong when getRunTime on first training: " + ex);
            }
        }

        return new ArrayList<>();
    }

    private static void setStaticVariables(String[] args){
        System.out.println("*********************************************");
        System.out.println("****************Set Variables****************");

        trainingStrings.add(trainingString);
        models.add(getModel.getModel(modelFileName));

        for (int i = 0; i<args.length; i++) {
            args[i] = args[i].toLowerCase();
        }
        if (args.length>1&& !Arrays.asList(args).contains("threshold") ){
            batchSize = Integer.parseInt(args[1]);
            if (!Arrays.asList(args).contains("quad")) {
                System.out.println("batchSize has been manually set to: " + batchSize);
            }
        }

        if (Arrays.asList(args).contains("infodens")| Arrays.asList(args).contains("id") | Arrays.asList(args).contains("information")) {
            infodens = true;
            System.out.println("Implement information density");
        }

        if (Arrays.asList(args).contains("gibbs")) {
            methodChoice = "gibbs";
            System.out.println("Method has been manually set to Gibbs");
        }
        else{
            System.out.println("Method has been set to default LC");
        }

        if (Arrays.asList(args).contains("threshold")) {
            int index = Arrays.asList(args).indexOf("threshold");
            if (args.length> index+1) {
                if (NumberUtils.isNumber(args[index + 1])) {
                    threshold = Double.parseDouble(args[index + 1]);

                }
            }
            else { threshold = 100;}
            batchSize = Integer.MAX_VALUE;
            System.out.println("Threshold batch size set to: "+threshold);
        }

        if (Arrays.asList(args).contains("random")) {
            random = true;
            System.out.println("Random run with batch size "+batchSize);
        }

        if (Arrays.asList(args).contains("stochastic")|Arrays.asList(args).contains("stoc")){
            trainingString[7]= "true";
            System.out.println("Epic uses stochastic training");
        }
        else{
            System.out.println("Epic uses non-stochastic training");
        }
        if (Arrays.asList(args).contains("quad")|Arrays.asList(args).contains("quadratic")) {
            quadraticBatchSize = true;
            originalBatchSize = batchSize;
            System.out.println("Quadratic batch size starting at: "+batchSize);

        }

        if(Arrays.asList(args).contains("error")){
            error = true;
            System.out.println("Error adjustment active. Relabeling included.");
        }

        if(Arrays.asList(args).contains("noise")){
            noiseCut = true;
            System.out.println("Noise reduction active. Batch cutting included");
        }
        if (Arrays.asList(args).contains("db")|Arrays.asList(args).contains("database")) {
            splitAndWriteDB(noise);
            boo = false;
            System.out.println("Database is rewritten");

        }

        if(Arrays.asList(args).contains("vote")){
            methodChoice = "vote";
            int nOfModels = 10;
            int index = Arrays.asList(args).indexOf("vote");
            if (args.length> index+1) {
                if (NumberUtils.isNumber(args[index + 1])) {
                    nOfModels = Integer.parseInt(args[index + 1]);

                }
            }
            for (int m = 0; m < nOfModels ; m++) {
                String [] childTrainingString = trainingString.clone();
                childTrainingString[1] = "data/children/childLabeledPool"+(m+1)+".conll";
                childTrainingString[5] = "data/child_model"+(m+1)+".ser.gz";
                trainingStrings.add(childTrainingString.clone());
                models.add(getModel.getModel("./data/child_model"+(m+1)+".ser.gz"));
            }
            System.out.println(nOfModels+" models set for " + methodChoice +" entropy");
        }

        if(Arrays.asList(args).contains("train")){
            Train(trainingStrings);
            try {
                PrintWriter pw = new PrintWriter(new FileOutputStream(
                        new File("data/labeledRunSize.txt"), true));
                pw.append(labeledPoolSize + "\n");
                pw.close();
            }
            catch(FileNotFoundException f){
                System.out.println("Couldn't save labeled run size " +f);
            }
            System.out.println("Run starts with training the model before batch selection");
        }

        System.out.println("****************Variables Set****************");
        System.out.println("*********************************************");



    }
}