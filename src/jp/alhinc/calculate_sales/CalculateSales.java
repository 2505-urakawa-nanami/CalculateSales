package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
	private static final String BRANCHSALES_NOT_CONSECTIVE = "売上ファイル名が連番になっていません";
	private static final String SALEAMOUNT_ERROR = "合計金額が10桁を超えました";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println(UNKNOWN_ERROR);
			return;
		}
		
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
		File[] files = new File(args[0]).listFiles();

		//先にファイルの情報を格納するList(ArrayList)を宣言する
		List<File> rcdFiles = new ArrayList<>();

		//filesの数だけ繰り返すことで、
		//指定したパスに存在するすべてのファイル（または、ディレクトリ）の数だけ繰り返す
		for (int i = 0; i < files.length; i++) {
			String fileName = files[i].getName();

			//matchesを使用してファイル名が「数字8桁.rcd」なのか判定します。
			if (files[i].isFile() && fileName.matches("^\\d{8}.rcd$")) {
				rcdFiles.add(files[i]);
			}

		}
		
		//売上ファイルを保持しているListをソート
		Collections.sort(rcdFiles);

		//売上ファイルが連番かどうか確認
		for (int i = 0; i < rcdFiles.size() -1; i++) {
			//int型に変換
			int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFiles.get(i + 1).getName().substring(0, 8));

			if ((latter - former) != 1) {
				System.out.println(BRANCHSALES_NOT_CONSECTIVE);
				return;
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
				List<String> contents = new ArrayList<>();
				while ((line = br.readLine()) != null) {
					contents.add(line);
				}
				
				//売上ファイルのフォーマットを確認
				if (contents.size() != 2) {
					System.out.println("<" + rcdFiles.get(i).getName() + ">のフォーマットが不正です");
					return;
				}
				
				//Mapに特定のkeyが存在するか確認
				if (!branchNames.containsKey(contents.get(0))) {
					System.out.println("<" + rcdFiles.get(i).getName() + ">の支店コードが不正です");
					return;
				}

				//売上金額が数字かどうか
				if (!contents.get(1).matches("^[0-9]*$")) {
					System.out.println(UNKNOWN_ERROR);
					return;
				}

				//売上ファイルから読み込んだ売上金額をMapに加算していくために、型の変換を行います。
				String branchCode = contents.get(0);
				long fileSale = Long.parseLong(contents.get(1));

				//読み込んだ売上金額を加算します。
				Long saleAmount = branchSales.get(branchCode) + fileSale;

				//売上金額の合計が10桁を超えていないかの確認
				if (saleAmount >= 10000000000L) {
					System.out.println(SALEAMOUNT_ERROR);
					return;
				}

				//加算した売上金額をMapに追加
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
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while ((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)

				String[] items = line.split(",");
				
				//支店定義ファイルのフォーマットが不正な場合
				if ((items.length != 2) || (!items[0].matches("^\\d{3}$"))) {
					System.out.println(FILE_INVALID_FORMAT);
					return false;
				}

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
