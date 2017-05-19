package webscraper;

import java.io.*;
import java.net.*;
import java.util.regex.*;
import java.util.*;
import java.util.logging.*;

public class WebScraper {

    private static final String TAG_EXPR = "((?:<s(?:cript|tyle).*?>(?:.)*?<\\/s(?:cript|tyle)>)|<(?:(?:.)*?)>|(&.{1,5};))";
    private static final Pattern FIRST_PARAMETER_PATTERN = Pattern.compile("htt(?:ps|p):\\/\\/");

    public static void main(String[] args) {
        ArrayList<String> linkList = new ArrayList();
        ArrayList<String> wordList = new ArrayList();
        boolean verbosityFlag = false;
        boolean findWords = false;
        boolean findSentences = false;
        boolean countCharacters = false;

        //Reading arguments
        if (args.length < 3) {
            System.out.print("Invalid input. Not enough arguments.\n"
                    + "Input example:\n"
                    + "http://www.cnn.com Greece,default –v –w –c –e");
            return;
        }

        Matcher fstParMatcher = FIRST_PARAMETER_PATTERN.matcher(args[0]);
        if (fstParMatcher.find()) {
            linkList.add(args[0]);
        } else {
            File links = new File(args[0]);
            String encoding = "utf-8";
            try {
                Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(links), encoding));
                Scanner scanner = new Scanner(reader);
                while (scanner.hasNextLine()) {
                    linkList.add(scanner.nextLine());
                }
                reader.close();
            } catch (UnsupportedEncodingException | FileNotFoundException ex) {
                Logger.getLogger(WebScraper.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(WebScraper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (args[1].contains(",")) {
            wordList.addAll(Arrays.asList(args[1].split(",")));
        } else {
            wordList.add(args[1]);
        }

        for (int i = 2; i < args.length; i++) {
            switch (args[i].substring(1)) {
                case "v":
                    verbosityFlag = true;
                    break;
                case "w":
                    findWords = true;
                    break;
                case "e":
                    findSentences = true;
                    break;
                case "c":
                    countCharacters = true;
                    break;
            }
        }

        if (findSentences && !findWords) {
            System.out.print("Invalid input. Can't.\n"
                    + "Input example:\n"
                    + "http://www.cnn.com Greece,default –v –w –c –e");
            return;
        }

        //Scrapping
        int linkCount = 0;
        long overallScrapingTime = 0;
        long overallProcessingTime = 0;
        for (String link : linkList) {
            ++linkCount;
            long startTime = System.currentTimeMillis();
            String content = open(link).replaceAll(TAG_EXPR, "\\.").replaceAll("\\s+", " ").replaceAll("\\.+", ".").replaceAll("(\\.(\\s|\\.)*\\.)", ".");
            long endScrapingTime = System.currentTimeMillis();
            System.out.println("\n" + link + "\n");
            if (verbosityFlag) {
                long scrapingTime = endScrapingTime - startTime;
                overallScrapingTime += scrapingTime;
                System.out.println("Time spent on scraping: " + scrapingTime + "ms.\n");
            }
            if (countCharacters) {
                System.out.println("Page № " + linkCount + " contains " + content.length() + " charaters.");
            }
            for (String word : wordList) {
                int numberOfOccurrences = countOccurrences(content, word);
                boolean wordIsFound = (numberOfOccurrences > 0);
                if (findWords) {
                    if (!wordIsFound) {
                        System.out.println("Page № " + linkCount + " doesn't contain the word " + word + ".");
                    } else {
                        System.out.println("Page № " + linkCount + " contains " + numberOfOccurrences + " occurrenses of the word " + word + ".");
                    }
                }
                if (findSentences && wordIsFound) {
                    System.out.println("The word " + word + " can be found in these sentences:\n");
                    int numberOfSentence = 0;
                    for (String sentence : findSentences(content, word)) {

                        System.out.println(++numberOfSentence + ") " + sentence);
                    }
                    System.out.println();

                }
            }
            if (verbosityFlag) {
                long processingTime = System.currentTimeMillis() - endScrapingTime;
                overallProcessingTime += processingTime;
                System.out.println("Time spent on processing: " + processingTime + "ms.");
            }
            System.out.println("__________________________________________________________________________");
        }
        if (verbosityFlag) {
            System.out.println("Overall scraping time: " + overallScrapingTime + "ms. \n"
                    + "Overall processing time: " + overallProcessingTime + "ms.");
        }
    }

    /*
    Method for getting html-code of a page by provided link and turning it into a String object
    returns string object with html-code
     */
    private static String open(String link) {
        String result = "";
        try {
            URL url = new URL(link);
            URLConnection conn = url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                result = result + inputLine;
            }
            br.close();

        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
        return result;
    }

    /*
    Method for counting occurrences of one string (word) in another (html)
    returns integer number of occurrences
     */
    private static int countOccurrences(String html, String word) {
        int count = 0;
        Pattern pattern = Pattern.compile("(\\.|\\s)" + word.toLowerCase() + "(\\.|\\s)");
        Matcher matcher = pattern.matcher(html.toLowerCase());
        while (matcher.find()) {
            ++count;
        }
        return count;
    }

    /*
    Method for finding sentences containing given word(word) in given text(content)
    returns an arraylist of sentences
     */
    private static ArrayList<String> findSentences(String content, String word) {
        ArrayList<String> result = new ArrayList();
        Pattern pattern = Pattern.compile("(?:\\.|\\?|!|\\?\\s|\\.\\s|!\\s|^)((([^!\\.\\?]*\\s|\\.)" + word.toLowerCase() + "(\\.|\\s[^!\\.\\?]*)(?:\\.|\\?|!|\\?\\s|\\.\\s|!\\s|$))|([^!\\.\\?]*\\s|\\.)?" + word.toLowerCase() + "(?:\\.|\\?|!|$))");
        Matcher matcher = pattern.matcher(content.toLowerCase());
        while (matcher.find()) {
            for (int i = 0; i < matcher.group(1).length() - 1; i++) {
                if (matcher.group(1).substring(i, i + 1).matches("[a-z]|[a-я]")) {
                    result.add(matcher.group(1).substring(i, i + 1).toUpperCase() + matcher.group(1).substring(i + 1, matcher.group(1).length()));
                    break;
                }
            }
        }
        return result;
    }
}