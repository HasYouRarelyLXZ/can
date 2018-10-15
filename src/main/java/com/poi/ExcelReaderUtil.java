package com.poi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

/**
 * @author y
 * @create 2018-01-19 0:13
 * @desc
 **/
public class ExcelReaderUtil {
	//excel2003扩展名
	public static final String EXCEL03_EXTENSION = ".xls";
	//excel2007扩展名
	public static final String EXCEL07_EXTENSION = ".xlsx";

	public static List<OldAccountingVoucher> list=new ArrayList<OldAccountingVoucher>();
	/**
	 * 每获取一条记录，即打印
	 * 在flume里每获取一条记录即发送，而不必缓存起来，可以大大减少内存的消耗，这里主要是针对flume读取大数据量excel来说的
	 * @param sheetName
	 * @param sheetIndex
	 * @param curRow
	 * @param cellList
	 */
	public static void sendRows(String filePath, String sheetName, int sheetIndex, int curRow, List<String> cellList) {
			StringBuffer oneLineSb = new StringBuffer();
			oneLineSb.append(filePath);
			oneLineSb.append("--");
			oneLineSb.append("sheet" + sheetIndex);
			oneLineSb.append("::" + sheetName);//加上sheet名
			oneLineSb.append("--");
			oneLineSb.append("row" + curRow);
			oneLineSb.append("::");
			for (String cell : cellList) {
				oneLineSb.append(cell.trim());
				oneLineSb.append("|");
			}
			String oneLine = oneLineSb.toString();
			if (oneLine.endsWith("|")) {
				oneLine = oneLine.substring(0, oneLine.lastIndexOf("|"));
			}	//去除最后一个分隔符
			//System.out.println(oneLine);
			list = setListData(cellList, ExcelReaderUtil.list);
			//System.out.println(list.size());
	}

	public static Map<Integer,List<OldAccountingVoucher>> readExcel(String fileName) throws Exception {
		Map<Integer, List<OldAccountingVoucher>> map = new HashMap<Integer, List<OldAccountingVoucher>>();
		int totalRows =0;
		if (fileName.endsWith(EXCEL03_EXTENSION)) { //处理excel2003文件
			ExcelXlsReader excelXls=new ExcelXlsReader();
			totalRows =excelXls.process(fileName);
		} else if (fileName.endsWith(EXCEL07_EXTENSION)) {//处理excel2007文件
			ExcelXlsxReaderWithDefaultHandler excelXlsxReader = new ExcelXlsxReaderWithDefaultHandler();
			totalRows = excelXlsxReader.process(fileName);
			map.put(totalRows,list);
		} else {
			throw new Exception("文件格式错误，fileName的扩展名只能是xls或xlsx。");
		}
		//System.out.println("发送的总行数：" + totalRows);
		return  map;
	}

	public static void copyToTemp(File file,String tmpDir) throws Exception{
		FileInputStream fis=new FileInputStream(file);
		File file1=new File(tmpDir);
		if (file1.exists()){
			file1.delete();
		}
		FileOutputStream fos=new FileOutputStream(tmpDir);
		byte[] b=new byte[1024];
		int n=0;
		while ((n=fis.read(b))!=-1){
			fos.write(b,0,n);
		}
		fis.close();
		fos.close();
	}

	/*
	* 从cellList中拿到单元格数据并存到List<OldVoucher>
	* */
	public static List<OldAccountingVoucher> setListData(List<String> cellList,List<OldAccountingVoucher> list){
		int size = cellList.size();
		OldAccountingVoucher oldVoucher = new OldAccountingVoucher();
		if(size==10){
			// 课题所属部门名称
			oldVoucher.setTOrganization(cellList.get(9).toString());
		}
		// 日期
		oldVoucher.setVDate(cellList.get(0).toString());
		// 凭证编号
		oldVoucher.setVNo(cellList.get(1).toString());
		// 摘要
		oldVoucher.setVAbstract(cellList.get(2).toString());
		// 借方金额
		oldVoucher.setVDebitAmount(cellList.get(3).toString());
		// 贷方金额
		oldVoucher.setVCreditAmount(cellList.get(4).toString());
		// 课题段代码
		oldVoucher.setTNo(cellList.get(5).toString());
		// 课题段名称
		oldVoucher.setTName(cellList.get(6).toString());
		// 六级科目代码
		oldVoucher.setSNo(cellList.get(7).toString());
		// 六级科目名称
		oldVoucher.setSName(cellList.get(8).toString());
		list.add(oldVoucher);
		return  list;
	}


	public static void main(String[] args) throws Exception {
		long start=System.currentTimeMillis();
		String path="C:\\Users\\15176\\Desktop\\can\\2017全年账（平-格式已改)0712.xlsx";
		Map<Integer, List<OldAccountingVoucher>> map = ExcelReaderUtil.readExcel(path);
		Set<Integer> integers = map.keySet();
		for (Integer integer : integers) {
			List<OldAccountingVoucher> vouchers = map.get(integer);
			for (OldAccountingVoucher voucher : vouchers) {
				System.out.println(voucher);
			}
			System.out.println(vouchers.size());
		}
		System.out.println("我要离职了");
		long end=System.currentTimeMillis();
		System.out.println(end-start);
	}
}
