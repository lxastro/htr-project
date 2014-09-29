package xlong.data.tokenizer;

import java.util.LinkedList;
import java.util.List;

import weka.core.stemmers.Stemmer;
import xlong.nlp.wordsegmentation.WordSegmenter;
import xlong.nlp.wordsegmentation.WordSegmenters;

public class SingleWordSegmentStemmingTokenizer extends Tokenizer {

	private static final String DELIMITERSREG = "[0-9_\\W]";
	private static Stemmer stemmer =  new weka.core.stemmers.NullStemmer();;
	private static WordSegmenter wordSegmenter = WordSegmenters.getPeterNorvigBigramSegmenter();
	private static int mode = 0;

	private static void initStemmer(int newMode) {
		if (mode != newMode) {
			mode = newMode;
			switch (mode) {
			case 0:
				stemmer = new weka.core.stemmers.NullStemmer();
				break;
			case 1:
				stemmer = new weka.core.stemmers.SnowballStemmer();
				break;
			default:
				stemmer = new weka.core.stemmers.PTStemmer();
				break;
			}
		}
	}
	public SingleWordSegmentStemmingTokenizer(int mode) {
		super(null);
		initStemmer(mode);
	}

	public SingleWordSegmentStemmingTokenizer(int mode, Tokenizer father) {
		super(father);
		initStemmer(mode);
	}

	private List<String> processWords(String[] splitString) {
		LinkedList<String> clean = new LinkedList<String>();

		for (int i = 0; i < splitString.length; i++) {
			for (String word:wordSegmenter.segment(splitString[i].trim())) {
				word = stemmer.stem(word);
				clean.add(word);
			}
		}
		return clean;
	}

	@Override
	public List<String> myTokenize(String text) {
		return processWords(text.toLowerCase().split(DELIMITERSREG));
	}

	public static void main(String[] args) {
		String testString = "This is the test string. Test single word stemmer tokenizers. And a URL: http://www.nfl.com/teams/greenbaypackers/profile?team=GB";
		for (String word : new SingleWordSegmentStemmingTokenizer(0).tokenize(new String(testString))) {
			System.out.print(word + " ");
		}
		System.out.println();
		for (String word : new SingleWordSegmentStemmingTokenizer(1).tokenize(new String(testString))) {
			System.out.print(word + " ");
		}
		System.out.println();
		for (String word : new SingleWordSegmentStemmingTokenizer(2).tokenize(new String(testString))) {
			System.out.print(word + " ");
		}
		System.out.println();
	}

}
