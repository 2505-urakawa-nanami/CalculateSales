package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if (!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)

		//listfilesを使用してfilesという配列に、
		//指定したパスに存在するすべてのファイル(または、ディレクトリ)も情報を;格納
		String path = args[0];
		File[] files = new File(path).listFiles();

		//先にファイルの女王を格納するList(ArrayList)を宣言する
		List<File> rcdFiles = new ArrayList<>();

		//filesの数だけ繰り返すことで、
		//指定したパスに存在するすべてのファイル（または、ディレクトリ）の数だけ繰り返す
		for (int i = 0; i < files.length; i++) {
			String fileName = files[i].getName();

			//matchesを使用してファイル名が「数字8桁.rcd」なのか判定します。
			if (fileName.matches("^\\d{8}+.rcd$")) {
				rcdFiles.add(files[i]);
			}
		}

		//rcdFilesに複数の売上ファイルの情報を格納しているので、その数だけ繰り返す
		for (int i = 0; i < rcdFiles.size(); i++) {
			FileReader fr = null;
			BufferedReader br = null;
			try {
				File file = rcdFiles.get(i);
				fr = new FileReader(file);
				br = new BufferedReader(fr);
				String line;
				//while文が回った数だけ読み取り情報の確認（2行）
				List<String> list = new ArrayList<>();
				while ((line = br.readLine()) != null) {
					list.add(line);
				}
				//売上ファイルから読み込んだ売上金額をMapに加算していくために、型の変換を行います。
				String branchCode = list.get(0);
				long fileSale = Long.parseLong(list.get(1));

				//読み込んだ売上⾦額を加算します。
				Long saleCode = branchSales.get(branchCode);

				Long saleAmount = saleCode + fileSale;

				//加算した売上⾦額をMapに追加
				branchSales.put(branchCode, saleAmount);

			} catch (IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;
			} finally {
				//ファイルを開いているとき
				if (br != null) {
					try {
						//ファイルを閉じる
						br.close();
					} catch (IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}
		}
		// 支店別集計ファイル書き込み処理
		if (!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}
		return;
	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames,
			Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);

			//支店定義ファイルの存在確認
			if (!file.exists()) {
				System.out.println(FILE_NOT_EXIST);
				return false;
			}
			//支店定義ファイルのフォーマットが不正な場合
			//if((.length != 2))
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while ((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)

				String[] items = line.split(",");

				//Mapに追加する2つの情報をputの引数として指定
				branchNames.put(items[0], items[1]);
				branchSales.put(items[0], 0L);
			}
		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if (br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames,
			Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		BufferedWriter bw = null;
		try {
			//ファイルの作成
			File file = new File(path, fileName);
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);

			//Mapから全てのKey取得
			for (String key : branchNames.keySet()) {
				bw.write(key + "," + branchNames.get(key) + "," + branchSales.get(key));
				bw.newLine();
			}
		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			//ファイルを開いているとき
			if (bw != null) {
				try {
					//ファイルを閉じる
					bw.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}
}
