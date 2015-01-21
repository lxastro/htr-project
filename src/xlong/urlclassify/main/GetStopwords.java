package xlong.urlclassify.main;

import xlong.wm.sample.converter.TextToSparseVectorConverter;

public class GetStopwords {
	public static void main(String[] args) throws Exception{
		
		String[] stopWordsFiles = new String[] {
			"E:/longx/data/stop-words/stop-words_english_1_en.txt",
			"E:/longx/data/stop-words/stop-words_english_2_en.txt",
			"E:/longx/data/stop-words/stop-words_english_3_en.txt",
			"E:/longx/data/stop-words/stop-words_english_4_google_en.txt",
			"E:/longx/data/stop-words/stop-words_english_5_en.txt",
			"E:/longx/data/stop-words/stop-words_english_6_en.txt",
			"E:/longx/data/lxdata/stopwords1.txt",
			"E:/longx/data/lxdata/stopwords2.txt",
			"E:/longx/data/lxdata/stopwordsMySQL.txt",
			"E:/longx/data/lxdata/stopwordsUrl.txt",
		};
		for (String stopWordsFile:stopWordsFiles) {
			TextToSparseVectorConverter.addStopwords(stopWordsFile);
		}
		TextToSparseVectorConverter.writeStopwords("stopwords.txt");
	}
}
