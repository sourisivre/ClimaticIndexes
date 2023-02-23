package climaticIndexes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


/* APRESENTAÇÃO
 * 	
 * 	Este aplicativo baixa as series históricas dos índices climatológicos 
 * 	hospedados no site https://psl.noaa.gov.
 * 
 * 	Ao fazer o download, este aplicativo verifica a consistência dos valores,
 * 	remove erros e analisa inconsistências.
 * 
 * 	Este programa também realiza a interseção dos dados históricos para encontrar
 * 	um período de tempo onde seja disponibilizada valores paralelos de dados entre
 * 	os diferentes índices.
 * 
 * 	Como se trata de um software desenvolvido para um fim específico, ele
 * 	apresenta uma série de pontos de controle para debug.
 * 
 * 	Os arquivos necessários para o seu funcionamento são:
 * 		1- Arquivo de texto (iURL.txt) com a lista dos índices e dos seus respectivos
 * 			endereços de web
 * 
 * 	São 41 índices ao todo.
 * 
*/
public class ClimaticIndexes {
	
	public void checkInputData (ArrayList<String> inputDataArrayList) {
		
		int size = inputDataArrayList.size();
		
		//First Test: is it odd in number?
		if(inputDataArrayList.size()%2==0) {
			System.out.println("Input Data is paired! Ok.");
			//Second Test: duplicated entries
			for (int i=0; i<size/2; i++) {
				if(i==0) {
					String entryString = inputDataArrayList.get(2*i);
					String[] splitedString = entryString.split("/");
					if(splitedString.length!=3) {
						System.out.println("Entry line "+i+1+" is NOT correctly formated!");
					} else {
						String entryNextString = inputDataArrayList.get(2*(i+1));
						String[] splitedNextString = entryNextString.split("/");
						if(Integer.parseInt(splitedString[1])==Integer.parseInt(splitedNextString[1]) &
								Integer.parseInt(splitedString[2])==Integer.parseInt(splitedNextString[2])) {
							System.out.println("There is a duplicate at "+i+1+" line!");
						}
					}
				} else {
					String entryString = inputDataArrayList.get(2*i);
					String[] splitedString = entryString.split("/");
					if(splitedString.length!=3) {
						System.out.println("Entry line "+i+1+" is NOT correctly formated!");
					} else {
						String entryPreviousString = inputDataArrayList.get(2*(i-1));
						String[] splitedPreviousString = entryPreviousString.split("/");
						if(Integer.parseInt(splitedString[1])==Integer.parseInt(splitedPreviousString[1]) &
								Integer.parseInt(splitedString[2])==Integer.parseInt(splitedPreviousString[2])) {
							System.out.println("There is a duplicate at "+i+1+" line!");
						}
					}
				}
			}
		} else {
			System.out.println("!!! Input data is NOT paired!");
		}
		
		
		String inputBeginDateString = inputDataArrayList.get(0);
		String inputEndDateString = inputDataArrayList.get(size-2);
		
		int inputMonthBegin=0, inputYearBegin=0, inputMonthEnd=0, inputYearEnd=0;
		
		inputMonthBegin = Integer.parseInt(inputBeginDateString.substring(3, 5));
		inputYearBegin = Integer.parseInt(inputBeginDateString.substring(6, 10));
		inputMonthEnd = Integer.parseInt(inputEndDateString.substring(3, 5));
		inputYearEnd = Integer.parseInt(inputEndDateString.substring(6, 10));
		
		//Third Test: missing entries
		for (int i=inputYearBegin; i<inputYearEnd+1; i++) {
			if(i==inputYearBegin) {
				for(int j=inputMonthBegin; j<12+1; j++) {
					isThisEntryIntoInputData(i, j, inputDataArrayList);
				}
			} else {
				if(i==inputYearEnd) {
					for(int j=1; j<inputMonthEnd+1; j++) {
						isThisEntryIntoInputData(i, j, inputDataArrayList);
					}
				} else {
					for(int j=1; j<12+1; j++) {
						isThisEntryIntoInputData(i, j, inputDataArrayList);
					}
				}
			}
		}
		
		System.out.println("End of check for missing entries!");
	}
	
	public void isThisEntryIntoInputData (int year, int month, ArrayList<String> inputDataArrayList) {
		boolean ans = false;
		int size = inputDataArrayList.size();
		
		for (int i=0; i<size/2; i++) {
			int inputMonth, inputYear;
			inputMonth = Integer.parseInt(inputDataArrayList.get(2*i).substring(3,5));
			inputYear = Integer.parseInt(inputDataArrayList.get(2*i).substring(6,10));
			
			if(inputMonth==month & inputYear==year) {
				ans=true;
			}
		}
		
		if(ans==false) {
			System.out.println(year+"/"+month+" not found!");
		}
	}
	
	public ArrayList<String> populateiURL() {
		//[1] IMPORT LIST OF CLIMATOLOGICAL INDEXES FROM TXT FILE
		ArrayList <String> iURL = new ArrayList<>();
		
		try {
			File iURLtxt = new File(System.getProperty("user.dir")+"\\src\\files\\iURL.txt");
			Scanner scanner = new Scanner(iURLtxt);
			scanner.useDelimiter("\r\n");
			
			int count = 0;
			
			while(scanner.hasNext()) {
				String temp = scanner.next();
				iURL.add(temp);
				count++;
			}
			
			System.out.println("There was/were "+count/2+" URL index(es)!\n");
			scanner.close();
			return iURL;
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public String getIndexValuesFromURL (String url) {
		//[1] CONNECT TO NOAA URL ADDRESS AND COPY DATA TO A STRING
		try {
			Document doc = (Document) Jsoup.connect(url).userAgent("Experimental Software - custom3300@gmail.com").get();
			Element bodyElement = doc.body();
			String bodyString = bodyElement.toString();
			String bodyWithoutTagString = bodyString.replace("<body>","").replace("</body>","").replace("\\n", "").trim();
			return bodyWithoutTagString;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	
	public ArrayList<String> prepareData (String htmlString) {
		int initialYear, finalYear;
		ArrayList<String> outputArrayList = new ArrayList<>();
		String[] splitedStrings = htmlString.split(" ");
		
		//[1]CHECK CONSISTENCY OF IMPORTED DATA - CORRECT DATA WITH INCORRECT FORMAT
		int n = splitedStrings.length;
		ArrayList<String> consistedSpplitedArrayList = new ArrayList<>();
		ArrayList<String> cleanedSplitedArrayList = new ArrayList<>();
		
		for (int i=0; i<n; i++) {
			String tempString = splitedStrings[i];
			String[] checkString = tempString.split("-");
			
			if (checkString.length==1) {
				consistedSpplitedArrayList.add(tempString);
				//TODO REMOVE
				//System.out.println(tempString);
			} else {
				if (checkString.length==2) {
					if(checkString[0].length()==0) {
						String newString = "-"+checkString[1];
						consistedSpplitedArrayList.add(newString);
						//TODO REMOVE
						//System.out.println(newString);
					} else {
						String newString = checkString[0];
						consistedSpplitedArrayList.add(newString);
						String newString2 = "-"+checkString[1];
						consistedSpplitedArrayList.add(newString2);
						//TODO REMOVE
						//System.out.println(newString);
						//System.out.println(newString2);
					}
				} else {
					//FIXME 
					//(02/09/2021) Erro encontrado ao aplicar o método ao endereço https://psl.noaa.gov/data/correlation/gmsst.data
					/*Log:
					Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: Index 1 out of bounds for length 0
					at climaticIndexes.ClimaticIndexes.prepareData(ClimaticIndexes.java:214)
					at climaticIndexes.ClimaticIndexes.importIndexData(ClimaticIndexes.java:287)
					at climaticIndexes.ClimaticIndexes.main(ClimaticIndexes.java:1520)
					*/
					String newString = "-"+checkString[1];
					consistedSpplitedArrayList.add(newString);
					String newString2 = "-"+checkString[2];
					consistedSpplitedArrayList.add(newString2);
					//TODO REMOVE
					System.out.println(newString);
					System.out.println(newString2);
				}
			}
		}
		
		//[2] CLEAR ARRAYLIST FROM TEXT
		int cn = consistedSpplitedArrayList.size();
		for (int i=0; i<cn; i++) {
			if(consistedSpplitedArrayList.get(i).matches("^([+-](?=\\.?\\d))?(\\d+)?(\\.\\d+)?$")) {
				cleanedSplitedArrayList.add(consistedSpplitedArrayList.get(i));
			} else {
				//Do nothing...
			}
		}
		
		initialYear = Integer.parseInt(cleanedSplitedArrayList.get(0));
		finalYear = Integer.parseInt(cleanedSplitedArrayList.get(1));
		
		//CLEAR ARRAYLIST FROM NULL VALUES
		/*int cn2 = cleanedSplitedArrayList.size();
		int nullValueIndex = (finalYear-initialYear)*12+(finalYear-initialYear)+12+2+1;
		String nullValueString = cleanedSplitedArrayList.get(nullValueIndex);
		ArrayList<String> cleanedSplitedArrayList2 = new ArrayList<>();
		
		for (int i=0; i<cn2; i++) {
			if(cleanedSplitedArrayList.get(i).contains(nullValueString)==false) {
				cleanedSplitedArrayList2.add(cleanedSplitedArrayList.get(i));
			} else {
				//Do nothing...
				System.out.println("Found: "+cleanedSplitedArrayList.get(i)+" at index: "+i);
			}
		}*/
		
		int mark = 2;
		
		//[3] MOUNT FINAL OUTPUT ARRAYLIST
		for (int i=initialYear; i<finalYear+1; i++) {
			for (int j=0; j<12; j++) {
				String dateString = String.valueOf(i)+"/"+String.valueOf(j+1);
				outputArrayList.add(dateString);
				outputArrayList.add(cleanedSplitedArrayList.get(mark+1));
				mark++;
			}
			mark++;
		}
		return outputArrayList;
	}
	
	public void importIndexData () throws Exception{
		/*[RESUME] PERFORM 
		 *  1- populateiURL();
		 *  2- getIndexValueFromURL();
		 *  3- prepareData();
		 *  4- Create TXT file;	
		 */
		
		ArrayList<String> iURLList = new ArrayList<>();
		iURLList = populateiURL();
		
		int nFiles = iURLList.size()/2;
		
		for (int i=0; i<nFiles; i++) {
			//TODO REMOVE
			//System.out.println("Downloading data for: TNI");
			//String indexHTMLString = getIndexValuesFromURL("https://psl.noaa.gov/data/correlation/tni.data");
			System.out.println("Downloading data for: "+iURLList.get(2*i)+" index at: "+iURLList.get(2*i+1));
			String indexHTMLString = getIndexValuesFromURL(iURLList.get(2*i+1));
			ArrayList<String> outputArrayList = prepareData(indexHTMLString);
			
			System.out.println("Creating file: "+System.getProperty("user.dir")+"\\src\\files\\"+iURLList.get(i*2)+".txt");
			File fileWriter = new File(System.getProperty("user.dir")+"\\src\\files\\"+iURLList.get(i*2)+".txt");
			PrintWriter printerPrintWriter = new PrintWriter(fileWriter);
			
			int sizeOutputArray = outputArrayList.size();
			
			System.out.println("Printing file...");
			
			for (int j=0; j<sizeOutputArray/2; j++) {
				printerPrintWriter.print(outputArrayList.get(2*j)+"\t"+outputArrayList.get(2*j+1)+"\n");
			}
			
			printerPrintWriter.close();
			System.out.println("File successfully created! Next one!\n");
		}
	}
	
	/* <<<ABORT>>>
	public void rewriteIndexDataForCorrelation() throws Exception{
		//[RESUME] PERFORM 
		//  1- populateiURL();
		//  2- getIndexValueFromURL();
		//  3- prepareData();
		//  4- Create TXT file;	

		
		ArrayList<String> iURLList = new ArrayList<>();
		iURLList = populateiURL();
		
		int nFiles = iURLList.size()/2;
		
		for (int i=0; i<nFiles; i++) {
			//TODO REMOVE
			//System.out.println("Downloading data for: TNI");
			//String indexHTMLString = getIndexValuesFromURL("https://psl.noaa.gov/data/correlation/tni.data");
			System.out.println("Downloading data for: "+iURLList.get(2*i)+" index at: "+iURLList.get(2*i+1));
			String indexHTMLString = getIndexValuesFromURL(iURLList.get(2*i+1));
			ArrayList<String> outputArrayList = prepareData(indexHTMLString);
			
			System.out.println("Creating file: "+System.getProperty("user.dir")+"\\src\\files\\correlation\\"+iURLList.get(i*2)+".txt");
			File fileWriter = new File(System.getProperty("user.dir")+"\\src\\files\\correlation\\"+iURLList.get(i*2)+".txt");
			PrintWriter printerPrintWriter = new PrintWriter(fileWriter);
			
			int sizeOutputArray = outputArrayList.size();
			
			System.out.println("Printing file...");
			
			for (int j=0; j<sizeOutputArray/2; j++) {
				printerPrintWriter.print(outputArrayList.get(2*j)+"\t"+outputArrayList.get(2*j+1)+"\n");
			}
			
			printerPrintWriter.close();
			System.out.println("File successfully created! Next one!\n");
		}
	}
	*/
	
	public ArrayList<String> getDataOfIndexArrayList (String path) throws Exception{
		
		//[1] READ TXT FILE WITH INDEX VALUES THROUGH FILE ADDRESS
		File file = new File(path);
		Scanner scanner = new Scanner(file);
		scanner.useDelimiter("\t|\r\n|\n");
		ArrayList<String> outputArrayList = new ArrayList<>();
		
		while(scanner.hasNext()) {
			outputArrayList.add(scanner.next());
		}
		
		scanner.close();
		return outputArrayList;
	}
	
	public String performAnalysis (ArrayList<String> inputDataArrayList, String pathString) throws Exception{
		
		//[1] GET BOUNDARIES FROM LISTS
		int inputSize = inputDataArrayList.size();
		ArrayList<String> indexArrayList = getDataOfIndexArrayList(pathString);
		int indexSize = indexArrayList.size();
		
		String inputBeginDateString = inputDataArrayList.get(0);
		String inputEndDateString = inputDataArrayList.get(inputSize-2);
		String indexBeginDateString = indexArrayList.get(0);
		String indexEndDateString = indexArrayList.get(indexSize-2);
		
		//TODO REMOVE
		//System.out.println(inputBeginDateString);
		//System.out.println(inputEndDateString);
		//System.out.println(indexBeginDateString);
		//System.out.println(indexEndDateString);

		int inputMonthBegin=0, inputYearBegin=0, inputMonthEnd=0, inputYearEnd=0;
		int indexMonthBegin=0, indexYearBegin=0, indexMonthEnd=0, indexYearEnd=0;
		
		inputMonthBegin = Integer.parseInt(inputBeginDateString.substring(3, 5));
		inputYearBegin = Integer.parseInt(inputBeginDateString.substring(6, 10));
		inputMonthEnd = Integer.parseInt(inputEndDateString.substring(3, 5));
		inputYearEnd = Integer.parseInt(inputEndDateString.substring(6, 10));

		if(indexBeginDateString.length()==6) {
			indexMonthBegin = Integer.parseInt(indexBeginDateString.substring(5,6));
			indexYearBegin = Integer.parseInt(indexBeginDateString.substring(0,4));
		} else {
			if(indexBeginDateString.length()==7) {
				indexMonthBegin = Integer.parseInt(indexBeginDateString.substring(5,7));
				indexYearBegin = Integer.parseInt(indexBeginDateString.substring(0,4));
			}
		}
		
		if(indexEndDateString.length()==6) {
			indexMonthEnd = Integer.parseInt(indexEndDateString.substring(5,6));
			indexYearEnd = Integer.parseInt(indexEndDateString.substring(0,4));
		} else {
			if(indexEndDateString.length()==7) {
				indexMonthEnd = Integer.parseInt(indexEndDateString.substring(5,7));
				indexYearEnd = Integer.parseInt(indexEndDateString.substring(0,4));
			}
		}
		
		//TODO REMOVE
		//System.out.println(inputMonthBegin);
		//System.out.println(inputYearBegin);
		//System.out.println(inputMonthEnd);
		//System.out.println(inputYearEnd);
		
		//System.out.println(indexMonthBegin);
		//System.out.println(indexYearBegin);
		//System.out.println(indexMonthEnd);
		//System.out.println(indexYearEnd);
	
		ArrayList<String> boundarieStrings = new ArrayList<>();
		
		if(inputYearBegin==indexYearBegin) {
			boundarieStrings.add(String.valueOf(indexYearBegin));
			if(inputMonthBegin<=indexMonthBegin) {
				boundarieStrings.add(String.valueOf(indexMonthBegin));
			} else {
				boundarieStrings.add(String.valueOf(inputMonthBegin));
			}
		} else {
			if(inputYearBegin<indexYearBegin) {
				boundarieStrings.add(String.valueOf(indexYearBegin));
				boundarieStrings.add(String.valueOf(indexMonthBegin));
			} else {
				boundarieStrings.add(String.valueOf(inputYearBegin));
				boundarieStrings.add(String.valueOf(inputMonthBegin));
			}
		}
		
		if(inputYearEnd==indexYearEnd) {
			boundarieStrings.add(String.valueOf(indexYearEnd));
			if(inputMonthEnd>=indexMonthEnd) {
				boundarieStrings.add(String.valueOf(indexMonthEnd));
			} else {
				boundarieStrings.add(String.valueOf(inputMonthEnd));
			}
		} else {
			if(inputYearEnd>indexYearEnd) {
				boundarieStrings.add(String.valueOf(indexYearEnd));
				boundarieStrings.add(String.valueOf(indexMonthEnd));
			} else {
				boundarieStrings.add(String.valueOf(inputYearEnd));
				boundarieStrings.add(String.valueOf(inputMonthEnd));
			}
		}
		
		//[2] MOUNT TEMPORAL-EQUIVALENT LISTS
		ArrayList<String> equivalentDataArrayList = new ArrayList<>();
		ArrayList<String> equivalentInputArrayList = new ArrayList<>();
		ArrayList<String> equivalentIndexArrayList = new ArrayList<>();
		
		//CHECK IF LINEDATA IS INSIDE BOUNDARIES
		for (int i=0; i<(inputSize/2); i++) {
			String lineDateString = inputDataArrayList.get(2*i);
			boolean isItIn = isOkANA(boundarieStrings, lineDateString);
			
			if(isItIn==true) {
				equivalentDataArrayList.add(lineDateString);
				equivalentInputArrayList.add(inputDataArrayList.get(2*i+1));
			}
		}
		
		for (int i=0; i<(indexSize/2); i++) {
			String lineDateString = indexArrayList.get(2*i);
			boolean isItIn = isOkNOAA(boundarieStrings, lineDateString);
			
			if(isItIn==true) {
				equivalentIndexArrayList.add(indexArrayList.get(2*i+1));
			}
		}
		/*
		//[3] SET DIFFERENT LISTS FOR LAG AND TRIMESTRAL COMPARISON
		int initialYear = Integer.parseInt(boundarieStrings.get(0));
		int finalYear = Integer.parseInt(boundarieStrings.get(2));
		@SuppressWarnings("unused")
		int initialMonth = Integer.parseInt(boundarieStrings.get(1));
		int finalMonth = Integer.parseInt(boundarieStrings.get(3));
		int range = finalYear-initialYear;
		
		//LAG 0 - JFM/AMJ/JAS/OND
		//JFM LAG0
		ArrayList<String> jfmLag0InputArrayList = new ArrayList<>();
		ArrayList<String> jfmLag0IndexArrayList = new ArrayList<>();
		for(int i=0; i<range+1; i++) {
			if(i+initialYear==finalYear) {
				if((0+1)<=finalMonth) {
					jfmLag0InputArrayList.add(equivalentInputArrayList.get((12*i)+0));
					jfmLag0IndexArrayList.add(equivalentIndexArrayList.get((12*i)+0));
				}
				if((1+1)<=finalMonth) {
					jfmLag0InputArrayList.add(equivalentInputArrayList.get((12*i)+1));
					jfmLag0IndexArrayList.add(equivalentIndexArrayList.get((12*i)+1));
				}
				if((2+1)<=finalMonth) {
					jfmLag0InputArrayList.add(equivalentInputArrayList.get((12*i)+2));
					jfmLag0IndexArrayList.add(equivalentIndexArrayList.get((12*i)+2));
				}
				
			} else {
				jfmLag0InputArrayList.add(equivalentInputArrayList.get((12*i)+0));
				jfmLag0InputArrayList.add(equivalentInputArrayList.get((12*i)+1));
				jfmLag0InputArrayList.add(equivalentInputArrayList.get((12*i)+2));
				
				jfmLag0IndexArrayList.add(equivalentIndexArrayList.get((12*i)+0));
				jfmLag0IndexArrayList.add(equivalentIndexArrayList.get((12*i)+1));
				jfmLag0IndexArrayList.add(equivalentIndexArrayList.get((12*i)+2));
			}
			//TODO REMOVER
			//System.out.println(i);
			//System.out.println(i+initialYear);
		}
		
		//AMJ LAG0
		ArrayList<String> amjLag0InputArrayList = new ArrayList<>();
		ArrayList<String> amjLag0IndexArrayList = new ArrayList<>();
		for(int i=0; i<range+1; i++) {
			if(i+initialYear==finalYear) {
				if((3+1)<=finalMonth) {
					amjLag0InputArrayList.add(equivalentInputArrayList.get((12*i)+3));
					amjLag0IndexArrayList.add(equivalentIndexArrayList.get((12*i)+3));
				}
				if((4+1)<=finalMonth) {
					amjLag0InputArrayList.add(equivalentInputArrayList.get((12*i)+4));
					amjLag0IndexArrayList.add(equivalentIndexArrayList.get((12*i)+4));
				}
				if((5+1)<=finalMonth) {
					amjLag0InputArrayList.add(equivalentInputArrayList.get((12*i)+5));
					amjLag0IndexArrayList.add(equivalentIndexArrayList.get((12*i)+5));
				}
				
			} else {
				amjLag0InputArrayList.add(equivalentInputArrayList.get((12*i)+3));
				amjLag0InputArrayList.add(equivalentInputArrayList.get((12*i)+4));
				amjLag0InputArrayList.add(equivalentInputArrayList.get((12*i)+5));
				
				amjLag0IndexArrayList.add(equivalentIndexArrayList.get((12*i)+3));
				amjLag0IndexArrayList.add(equivalentIndexArrayList.get((12*i)+4));
				amjLag0IndexArrayList.add(equivalentIndexArrayList.get((12*i)+5));
			}
		}
		
		//JAS LAG0
		ArrayList<String> jasLag0InputArrayList = new ArrayList<>();
		ArrayList<String> jasLag0IndexArrayList = new ArrayList<>();
		for(int i=0; i<range+1; i++) {
			if(i+initialYear==finalYear) {
				if((6+1)<=finalMonth) {
					jasLag0InputArrayList.add(equivalentInputArrayList.get((12*i)+6));
					jasLag0IndexArrayList.add(equivalentIndexArrayList.get((12*i)+6));
				}
				if((7+1)<=finalMonth) {
					jasLag0InputArrayList.add(equivalentInputArrayList.get((12*i)+7));
					jasLag0IndexArrayList.add(equivalentIndexArrayList.get((12*i)+7));
				}
				if((8+1)<=finalMonth) {
					jasLag0InputArrayList.add(equivalentInputArrayList.get((12*i)+8));
					jasLag0IndexArrayList.add(equivalentIndexArrayList.get((12*i)+8));
				}
				
			} else {
				jasLag0InputArrayList.add(equivalentInputArrayList.get((12*i)+6));
				jasLag0InputArrayList.add(equivalentInputArrayList.get((12*i)+7));
				jasLag0InputArrayList.add(equivalentInputArrayList.get((12*i)+8));
				
				jasLag0IndexArrayList.add(equivalentIndexArrayList.get((12*i)+6));
				jasLag0IndexArrayList.add(equivalentIndexArrayList.get((12*i)+7));
				jasLag0IndexArrayList.add(equivalentIndexArrayList.get((12*i)+8));
			}
		}
		
		//OND LAG0
		ArrayList<String> ondLag0InputArrayList = new ArrayList<>();
		ArrayList<String> ondLag0IndexArrayList = new ArrayList<>();
		for(int i=0; i<range+1; i++) {
			if(i+initialYear==finalYear) {
				if((9+1)<=finalMonth){
					ondLag0InputArrayList.add(equivalentInputArrayList.get((12*i)+9));
					ondLag0IndexArrayList.add(equivalentIndexArrayList.get((12*i)+9));
				}
				if((10+1)<=finalMonth){
					ondLag0InputArrayList.add(equivalentInputArrayList.get((12*i)+10));
					ondLag0IndexArrayList.add(equivalentIndexArrayList.get((12*i)+10));
				}
				if((11+1)<=finalMonth){
					ondLag0InputArrayList.add(equivalentInputArrayList.get((12*i)+11));
					ondLag0IndexArrayList.add(equivalentIndexArrayList.get((12*i)+11));
				}
			} else {
				ondLag0InputArrayList.add(equivalentInputArrayList.get((12*i)+9));
				ondLag0IndexArrayList.add(equivalentIndexArrayList.get((12*i)+9));
				
				ondLag0InputArrayList.add(equivalentInputArrayList.get((12*i)+10));
				ondLag0IndexArrayList.add(equivalentIndexArrayList.get((12*i)+10));
				
				ondLag0InputArrayList.add(equivalentInputArrayList.get((12*i)+11));
				ondLag0IndexArrayList.add(equivalentIndexArrayList.get((12*i)+11));
			}
		}
		
		//LAG 1 - JFM/AMJ/JAS/OND
		//JFM LAG1
		ArrayList<String> jfmLag1MapArrayList = new ArrayList<>();
		ArrayList<String> jfmLag1InputArrayList = new ArrayList<>();
		ArrayList<String> jfmLag1IndexArrayList = new ArrayList<>();
		for (int i=1; i<range+1; i++) {
			if(i+initialYear==finalYear) {
				if((0+1)<=finalMonth) {
					jfmLag1MapArrayList.add(String.valueOf(12*i+0));
				}
				if((1+1)<=finalMonth) {
					jfmLag1MapArrayList.add(String.valueOf(12*i+1));
				}
				if((2+1)<=finalMonth) {
					jfmLag1MapArrayList.add(String.valueOf(12*i+2));
				}
				
			} else {
				jfmLag1MapArrayList.add(String.valueOf(12*i+0));
				jfmLag1MapArrayList.add(String.valueOf(12*i+1));
				jfmLag1MapArrayList.add(String.valueOf(12*i+2));
			}
		}
		
		int mapSize = jfmLag1MapArrayList.size();
		
		for (int i=0; i<mapSize; i++) {
			jfmLag1IndexArrayList.add(equivalentIndexArrayList.get(Integer.parseInt(jfmLag1MapArrayList.get(i))-12));
			jfmLag1InputArrayList.add(equivalentInputArrayList.get(Integer.parseInt(jfmLag1MapArrayList.get(i))));
		}
		
		//AMJ LAG1
		ArrayList<String> amjLag1MapArrayList = new ArrayList<>();
		ArrayList<String> amjLag1InputArrayList = new ArrayList<>();
		ArrayList<String> amjLag1IndexArrayList = new ArrayList<>();
		for (int i=1; i<range+1; i++) {
			if(i+initialYear==finalYear) {
				if((3+1)<=finalMonth) {
					amjLag1MapArrayList.add(String.valueOf(12*i+3));
				}
				if((4+1)<=finalMonth) {
					amjLag1MapArrayList.add(String.valueOf(12*i+4));
				}
				if((5+1)<=finalMonth) {
					amjLag1MapArrayList.add(String.valueOf(12*i+5));
				}
				
			} else {
				amjLag1MapArrayList.add(String.valueOf(12*i+3));
				amjLag1MapArrayList.add(String.valueOf(12*i+4));
				amjLag1MapArrayList.add(String.valueOf(12*i+5));
			}
		}
		
		mapSize = amjLag1MapArrayList.size();
		
		for (int i=0; i<mapSize; i++) {
			amjLag1IndexArrayList.add(equivalentIndexArrayList.get(Integer.parseInt(amjLag1MapArrayList.get(i))-12));
			amjLag1InputArrayList.add(equivalentInputArrayList.get(Integer.parseInt(amjLag1MapArrayList.get(i))));
		}
		
		//JAS LAG1
		ArrayList<String> jasLag1MapArrayList = new ArrayList<>();
		ArrayList<String> jasLag1InputArrayList = new ArrayList<>();
		ArrayList<String> jasLag1IndexArrayList = new ArrayList<>();
		for (int i=1; i<range+1; i++) {
			if(i+initialYear==finalYear) {
				if((6+1)<=finalMonth) {
					jasLag1MapArrayList.add(String.valueOf(12*i+6));
				}
				if((7+1)<=finalMonth) {
					jasLag1MapArrayList.add(String.valueOf(12*i+7));
				}
				if((8+1)<=finalMonth) {
					jasLag1MapArrayList.add(String.valueOf(12*i+8));
				}
				
			} else {
				jasLag1MapArrayList.add(String.valueOf(12*i+6));
				jasLag1MapArrayList.add(String.valueOf(12*i+7));
				jasLag1MapArrayList.add(String.valueOf(12*i+8));
			}
		}
		
		mapSize = jasLag1MapArrayList.size();
		
		for (int i=0; i<mapSize; i++) {
			jasLag1IndexArrayList.add(equivalentIndexArrayList.get(Integer.parseInt(jasLag1MapArrayList.get(i))-12));
			jasLag1InputArrayList.add(equivalentInputArrayList.get(Integer.parseInt(jasLag1MapArrayList.get(i))));
		}
		
		//OND LAG1
		ArrayList<String> ondLag1MapArrayList = new ArrayList<>();
		ArrayList<String> ondLag1InputArrayList = new ArrayList<>();
		ArrayList<String> ondLag1IndexArrayList = new ArrayList<>();
		for (int i=1; i<range+1; i++) {
			if(i+initialYear==finalYear) {
				if((9+1)<=finalMonth) {
					ondLag1MapArrayList.add(String.valueOf(12*i+9));
				}
				if((10+1)<=finalMonth) {
					ondLag1MapArrayList.add(String.valueOf(12*i+10));
				}
				if((11+1)<=finalMonth) {
					ondLag1MapArrayList.add(String.valueOf(12*i+11));
				}
				
			} else {
				ondLag1MapArrayList.add(String.valueOf(12*i+9));
				ondLag1MapArrayList.add(String.valueOf(12*i+10));
				ondLag1MapArrayList.add(String.valueOf(12*i+11));
			}
		}
		
		mapSize = ondLag1MapArrayList.size();
		
		for (int i=0; i<mapSize; i++) {
			ondLag1IndexArrayList.add(equivalentIndexArrayList.get(Integer.parseInt(ondLag1MapArrayList.get(i))-12));
			ondLag1InputArrayList.add(equivalentInputArrayList.get(Integer.parseInt(ondLag1MapArrayList.get(i))));
		}
		
		
		//[4] PERFOM R CALCULATION
		//First for equivalent series
		//Second for trimestral lag0 series
		//Third for trimestral lag1 series
		
		String cString = "Range: "+boundarieStrings.get(0)+"/"+boundarieStrings.get(1)+" - "+boundarieStrings.get(2)+"/"+boundarieStrings.get(3)+"\n"+
				"n = "+equivalentInputArrayList.size()+"\n"+
				"R = "+correlationString(equivalentInputArrayList, equivalentIndexArrayList)+"\n"+
				"JFM Lag0 = "+correlationString(jfmLag0InputArrayList, jfmLag0IndexArrayList)+"\tLag1 = "+
				correlationString(jfmLag1InputArrayList, jfmLag1IndexArrayList)+"\n"+
				"AMJ Lag0 = "+correlationString(amjLag0InputArrayList, amjLag0IndexArrayList)+"\tLag1 = "+
				correlationString(amjLag1InputArrayList, amjLag1IndexArrayList)+"\n"+
				"JAS Lag0 = "+correlationString(jasLag0InputArrayList, jasLag0IndexArrayList)+"\tLag1 = "+
				correlationString(jasLag1InputArrayList, jasLag1IndexArrayList)+"\n"+
				"OND Lag0 = "+correlationString(ondLag0InputArrayList, ondLag0IndexArrayList)+"\tLag1 = "+
				correlationString(ondLag1InputArrayList, ondLag1IndexArrayList);
		
		String rString = boundarieStrings.get(0)+"/"+boundarieStrings.get(1)+" - "+boundarieStrings.get(2)+"/"+boundarieStrings.get(3)+"\t"+
				equivalentInputArrayList.size()+"\t"+
				correlationString(equivalentInputArrayList, equivalentIndexArrayList)+"\t"+
				correlationString(jfmLag0InputArrayList, jfmLag0IndexArrayList)+"\t"+
				correlationString(amjLag0InputArrayList, amjLag0IndexArrayList)+"\t"+
				correlationString(jasLag0InputArrayList, jasLag0IndexArrayList)+"\t"+
				correlationString(ondLag0InputArrayList, ondLag0IndexArrayList)+"\t"+
				correlationString(jfmLag1InputArrayList, jfmLag1IndexArrayList)+"\t"+
				correlationString(amjLag1InputArrayList, amjLag1IndexArrayList)+"\t"+
				correlationString(jasLag1InputArrayList, jasLag1IndexArrayList)+"\t"+
				correlationString(ondLag1InputArrayList, ondLag1IndexArrayList);

		//CONSOLE OUTPUT
		System.out.println(cString);
		
		return rString;
		*/
		//<<OBSOLETE>>//
		
		int range = Integer.parseInt(boundarieStrings.get(2))-
					Integer.parseInt(boundarieStrings.get(0))+1;
		
		String compiledStrings[][][] = new String[range][12][3];
		
		int mark = 0;
		for (int i=0; i<range; i++) {
			for (int j=0; j<12; j++) {
				if(i+Integer.parseInt(boundarieStrings.get(0))==Integer.parseInt(boundarieStrings.get(2))){
					if(j+1<=Integer.parseInt(boundarieStrings.get(3))) {
						compiledStrings[i][j][0] = equivalentDataArrayList.get(mark);
						compiledStrings[i][j][1] = equivalentInputArrayList.get(mark);
						compiledStrings[i][j][2] = equivalentIndexArrayList.get(mark);
						mark++;
					} 
					if(j+1>Integer.parseInt(boundarieStrings.get(3))) {
						compiledStrings[i][j][0] = "00/00/0000";
						compiledStrings[i][j][1] = "empty";
						compiledStrings[i][j][2] = "empty";
					}
				} else {
					compiledStrings[i][j][0] = equivalentDataArrayList.get(mark);
					compiledStrings[i][j][1] = equivalentInputArrayList.get(mark);
					compiledStrings[i][j][2] = equivalentIndexArrayList.get(mark);
					mark++;
				}
			}
		}
		
		//LAG 0
		ArrayList<Double> avgJanToMayInputArrayList = new ArrayList<>();
		ArrayList<Double> avgJFMIndexArrayList = new ArrayList<>();
		ArrayList<Double> avgAMJIndexArrayList = new ArrayList<>();
		ArrayList<Double> avgJASIndexArrayList = new ArrayList<>();
		ArrayList<Double> avgONDIndexArrayList = new ArrayList<>();
		ArrayList<Double> avgJFMAMIndexArrayList = new ArrayList<>();
		ArrayList<Double> avgANNUALIndexArrayList = new ArrayList<>();
		
		for (int i=0; i<range; i++) {
				
			double sumJanToMayInput=0,
					sumJFMIndex = 0,
					sumAMJIndex = 0,
					sumJASIndex = 0,
					sumONDIndex = 0,
					sumJFMAMIndex = 0,
					sumAnnualIndex = 0;
			
			int nJanToMayInput=0,
				nJFMIndex = 0,
				nAMJIndex = 0,
				nJASIndex = 0,
				nONDIndex = 0,
				nJFMAMIndex = 0,
				nAnnualIndex = 0;
			
			for (int k=0; k<12; k++) {
				int month = Integer.parseInt(compiledStrings[i][k][0].substring(3,5));
				
				if(month==1){
					if(compiledStrings[i][k][0]=="empty") {
						//do nothing...
					} else {
						sumJanToMayInput = sumJanToMayInput+Double.parseDouble(compiledStrings[i][k][1]);
						sumJFMIndex = sumJFMIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumJFMAMIndex = sumJFMAMIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumAnnualIndex = sumAnnualIndex + Double.parseDouble(compiledStrings[i][k][2]);
						nJFMIndex++;
						nJFMAMIndex++;
						nJanToMayInput++;
						nAnnualIndex++;
					}
				}
				if(month==2){
					if(compiledStrings[i][k][0]=="empty") {
						//do nothing...
					} else {
						sumJanToMayInput = sumJanToMayInput+Double.parseDouble(compiledStrings[i][k][1]);
						sumJFMIndex = sumJFMIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumJFMAMIndex = sumJFMAMIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumAnnualIndex = sumAnnualIndex + Double.parseDouble(compiledStrings[i][k][2]);
						nJFMIndex++;
						nJFMAMIndex++;
						nJanToMayInput++;
						nAnnualIndex++;
					}
				}
				if(month==3){
					if(compiledStrings[i][k][0]=="empty") {
						//do nothing...
					} else {
						sumJanToMayInput = sumJanToMayInput+Double.parseDouble(compiledStrings[i][k][1]);
						sumJFMIndex = sumJFMIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumJFMAMIndex = sumJFMAMIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumAnnualIndex = sumAnnualIndex + Double.parseDouble(compiledStrings[i][k][2]);
						nJFMIndex++;
						nJFMAMIndex++;
						nJanToMayInput++;
						nAnnualIndex++;
					}
				}
				if(month==4){
					if(compiledStrings[i][k][0]=="empty") {
						//do nothing...
					} else {
						sumJanToMayInput = sumJanToMayInput+Double.parseDouble(compiledStrings[i][k][1]);
						sumAMJIndex = sumAMJIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumJFMAMIndex = sumJFMAMIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumAnnualIndex = sumAnnualIndex + Double.parseDouble(compiledStrings[i][k][2]);
						nAMJIndex++;
						nJFMAMIndex++;
						nJanToMayInput++;
						nAnnualIndex++;
					}
				}
				if(month==5){
					if(compiledStrings[i][k][0]=="empty") {
						//do nothing...
					} else {
						sumJanToMayInput = sumJanToMayInput+Double.parseDouble(compiledStrings[i][k][1]);
						sumAMJIndex = sumAMJIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumJFMAMIndex = sumJFMAMIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumAnnualIndex = sumAnnualIndex + Double.parseDouble(compiledStrings[i][k][2]);
						nAMJIndex++;
						nJFMAMIndex++;
						nJanToMayInput++;
						nAnnualIndex++;
					}
				}
				if(month==6){
					if(compiledStrings[i][k][0]=="empty") {
						//do nothing...
					} else {
						sumAMJIndex = sumAMJIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumAnnualIndex = sumAnnualIndex + Double.parseDouble(compiledStrings[i][k][2]);
						nAMJIndex++;
						nAnnualIndex++;
					}
				}
				if(month==7){
					if(compiledStrings[i][k][0]=="empty") {
						//do nothing...
					} else {
						sumJASIndex = sumJASIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumAnnualIndex = sumAnnualIndex + Double.parseDouble(compiledStrings[i][k][2]);
						nJASIndex++;
						nAnnualIndex++;
					}
				}
				if(month==8){
					if(compiledStrings[i][k][0]=="empty") {
						//do nothing...
					} else {
						sumJASIndex = sumJASIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumAnnualIndex = sumAnnualIndex + Double.parseDouble(compiledStrings[i][k][2]);
						nJASIndex++;
						nAnnualIndex++;
					}
				}
				if(month==9){
					if(compiledStrings[i][k][0]=="empty") {
						//do nothing...
					} else {
						sumJASIndex = sumJASIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumAnnualIndex = sumAnnualIndex + Double.parseDouble(compiledStrings[i][k][2]);
						nJASIndex++;
						nAnnualIndex++;
					}
				}
				if(month==10){
					if(compiledStrings[i][k][0]=="empty") {
						//do nothing...
					} else {
						sumONDIndex = sumONDIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumAnnualIndex = sumAnnualIndex + Double.parseDouble(compiledStrings[i][k][2]);
						nONDIndex++;
						nAnnualIndex++;
					}
				}
				if(month==11){
					if(compiledStrings[i][k][0]=="empty") {
						//do nothing...
					} else {
						sumONDIndex = sumONDIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumAnnualIndex = sumAnnualIndex + Double.parseDouble(compiledStrings[i][k][2]);
						nONDIndex++;
						nAnnualIndex++;
					}
				}
				if(month==12){
					if(compiledStrings[i][k][0]=="empty") {
						//do nothing...
					} else {
						sumONDIndex = sumONDIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumAnnualIndex = sumAnnualIndex + Double.parseDouble(compiledStrings[i][k][2]);
						nONDIndex++;
						nAnnualIndex++;
					}
				}
			}
			
			double avgJanToMayInput,
			avgJFMIndex,
			avgAMJIndex,
			avgJASIndex,
			avgONDIndex,
			avgJFMAMIndex,
			avgAnnualIndex;
	
			avgJanToMayInput = sumJanToMayInput/nJanToMayInput;
			avgJFMIndex = sumJFMIndex/nJFMIndex;
			avgAMJIndex = sumAMJIndex/nAMJIndex;
			avgJASIndex = sumJASIndex/nJASIndex;
			avgONDIndex = sumONDIndex/nONDIndex;
			avgJFMAMIndex = sumJFMAMIndex/nJFMAMIndex;
			avgAnnualIndex = sumAnnualIndex/nAnnualIndex;
			
			avgJanToMayInputArrayList.add(avgJanToMayInput);
			avgJFMIndexArrayList.add(avgJFMIndex);
			avgAMJIndexArrayList.add(avgAMJIndex);
			avgJASIndexArrayList.add(avgJASIndex);
			avgONDIndexArrayList.add(avgONDIndex);
			avgJFMAMIndexArrayList.add(avgJFMAMIndex);
			avgANNUALIndexArrayList.add(avgAnnualIndex);
		}
		
		//LAG 1
		ArrayList<Double> avgJanToMayInputArrayList1 = new ArrayList<>();
		ArrayList<Double> avgJFMIndexArrayList1 = new ArrayList<>();
		ArrayList<Double> avgAMJIndexArrayList1 = new ArrayList<>();
		ArrayList<Double> avgJASIndexArrayList1 = new ArrayList<>();
		ArrayList<Double> avgONDIndexArrayList1 = new ArrayList<>();
		ArrayList<Double> avgJFMAMIndexArrayList1 = new ArrayList<>();
		ArrayList<Double> avgANNUALIndexArrayList1 = new ArrayList<>();
		
		for (int i=0; i<(range-1); i++) {
			
			double sumJanToMayInput=0,
					sumJFMIndex = 0,
					sumAMJIndex = 0,
					sumJASIndex = 0,
					sumONDIndex = 0,
					sumJFMAMIndex = 0,
					sumAnnualIndex = 0;
			
			int nJanToMayInput=0,
				nJFMIndex = 0,
				nAMJIndex = 0,
				nJASIndex = 0,
				nONDIndex = 0,
				nJFMAMIndex = 0,
				nAnnualIndex = 0;
			
			for (int k=0; k<(12); k++) {
				int month = Integer.parseInt(compiledStrings[i][k][0].substring(3,5));
				
				if(month==1){
					if(compiledStrings[i][k][0]=="empty") {
						//do nothing...
					} else {
						sumJanToMayInput = sumJanToMayInput+Double.parseDouble(compiledStrings[i+1][k][1]);
						sumJFMIndex = sumJFMIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumJFMAMIndex = sumJFMAMIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumAnnualIndex = sumAnnualIndex + Double.parseDouble(compiledStrings[i][k][2]);
						nJFMIndex++;
						nJFMAMIndex++;
						nJanToMayInput++;
						nAnnualIndex++;
					}
				}
				if(month==2){
					if(compiledStrings[i][k][0]=="empty") {
						//do nothing...
					} else {
						sumJanToMayInput = sumJanToMayInput+Double.parseDouble(compiledStrings[i+1][k][1]);
						sumJFMIndex = sumJFMIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumJFMAMIndex = sumJFMAMIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumAnnualIndex = sumAnnualIndex + Double.parseDouble(compiledStrings[i][k][2]);
						nJFMIndex++;
						nJFMAMIndex++;
						nJanToMayInput++;
						nAnnualIndex++;
					}
				}
				if(month==3){
					if(compiledStrings[i][k][0]=="empty") {
						//do nothing...
					} else {
						sumJanToMayInput = sumJanToMayInput+Double.parseDouble(compiledStrings[i+1][k][1]);
						sumJFMIndex = sumJFMIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumJFMAMIndex = sumJFMAMIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumAnnualIndex = sumAnnualIndex + Double.parseDouble(compiledStrings[i][k][2]);
						nJFMIndex++;
						nJFMAMIndex++;
						nJanToMayInput++;
						nAnnualIndex++;
					}
				}
				if(month==4){
					if(compiledStrings[i][k][0]=="empty") {
						//do nothing...
					} else {
						sumJanToMayInput = sumJanToMayInput+Double.parseDouble(compiledStrings[i+1][k][1]);
						sumAMJIndex = sumAMJIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumJFMAMIndex = sumJFMAMIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumAnnualIndex = sumAnnualIndex + Double.parseDouble(compiledStrings[i][k][2]);
						nAMJIndex++;
						nJFMAMIndex++;
						nJanToMayInput++;
						nAnnualIndex++;
					}
				}
				if(month==5){
					if(compiledStrings[i][k][0]=="empty") {
						//do nothing...
					} else {
						sumJanToMayInput = sumJanToMayInput+Double.parseDouble(compiledStrings[i+1][k][1]);
						sumAMJIndex = sumAMJIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumJFMAMIndex = sumJFMAMIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumAnnualIndex = sumAnnualIndex + Double.parseDouble(compiledStrings[i][k][2]);
						nAMJIndex++;
						nJFMAMIndex++;
						nJanToMayInput++;
						nAnnualIndex++;
					}
				}
				if(month==6){
					if(compiledStrings[i][k][0]=="empty") {
						//do nothing...
					} else {
						sumAMJIndex = sumAMJIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumAnnualIndex = sumAnnualIndex + Double.parseDouble(compiledStrings[i][k][2]);
						nAMJIndex++;
						nAnnualIndex++;
					}
				}
				if(month==7){
					if(compiledStrings[i][k][0]=="empty") {
						//do nothing...
					} else {
						sumJASIndex = sumJASIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumAnnualIndex = sumAnnualIndex + Double.parseDouble(compiledStrings[i][k][2]);
						nJASIndex++;
						nAnnualIndex++;
					}
				}
				if(month==8){
					if(compiledStrings[i][k][0]=="empty") {
						//do nothing...
					} else {
						sumJASIndex = sumJASIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumAnnualIndex = sumAnnualIndex + Double.parseDouble(compiledStrings[i][k][2]);
						nJASIndex++;
						nAnnualIndex++;
					}
				}
				if(month==9){
					if(compiledStrings[i][k][0]=="empty") {
						//do nothing...
					} else {
						sumJASIndex = sumJASIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumAnnualIndex = sumAnnualIndex + Double.parseDouble(compiledStrings[i][k][2]);
						nJASIndex++;
						nAnnualIndex++;
					}
				}
				if(month==10){
					if(compiledStrings[i][k][0]=="empty") {
						//do nothing...
					} else {
						sumONDIndex = sumONDIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumAnnualIndex = sumAnnualIndex + Double.parseDouble(compiledStrings[i][k][2]);
						nONDIndex++;
						nAnnualIndex++;
					}
				}
				if(month==11){
					if(compiledStrings[i][k][0]=="empty") {
						//do nothing...
					} else {
						sumONDIndex = sumONDIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumAnnualIndex = sumAnnualIndex + Double.parseDouble(compiledStrings[i][k][2]);
						nONDIndex++;
						nAnnualIndex++;
					}
				}
				if(month==12){
					if(compiledStrings[i][k][0]=="empty") {
						//do nothing...
					} else {
						sumONDIndex = sumONDIndex + Double.parseDouble(compiledStrings[i][k][2]);
						sumAnnualIndex = sumAnnualIndex + Double.parseDouble(compiledStrings[i][k][2]);
						nONDIndex++;
						nAnnualIndex++;
					}
				}
			}
			
			double avgJanToMayInput,
			avgJFMIndex,
			avgAMJIndex,
			avgJASIndex,
			avgONDIndex,
			avgJFMAMIndex,
			avgAnnualIndex;
	
			avgJanToMayInput = sumJanToMayInput/nJanToMayInput;
			avgJFMIndex = sumJFMIndex/nJFMIndex;
			avgAMJIndex = sumAMJIndex/nAMJIndex;
			avgJASIndex = sumJASIndex/nJASIndex;
			avgONDIndex = sumONDIndex/nONDIndex;
			avgJFMAMIndex = sumJFMAMIndex/nJFMAMIndex;
			avgAnnualIndex = sumAnnualIndex/nAnnualIndex;
			
			avgJanToMayInputArrayList1.add(avgJanToMayInput);
			avgJFMIndexArrayList1.add(avgJFMIndex);
			avgAMJIndexArrayList1.add(avgAMJIndex);
			avgJASIndexArrayList1.add(avgJASIndex);
			avgONDIndexArrayList1.add(avgONDIndex);
			avgJFMAMIndexArrayList1.add(avgJFMAMIndex);
			avgANNUALIndexArrayList1.add(avgAnnualIndex);
		}
		
		String cString = "Range: "+boundarieStrings.get(0)+"/"+boundarieStrings.get(1)+" - "+boundarieStrings.get(2)+"/"+boundarieStrings.get(3)+"\n"+
				"n = "+equivalentInputArrayList.size()+"\n"+
				"LAG 0:\n"+
				"Jan-May JFM = "+correlationString(avgJanToMayInputArrayList, avgJFMIndexArrayList)+"\n"+
				"Jan-May AMJ = "+correlationString(avgJanToMayInputArrayList, avgAMJIndexArrayList)+"\n"+
				"Jan-May JAS = "+correlationString(avgJanToMayInputArrayList, avgJASIndexArrayList)+"\n"+
				"Jan-May OND = "+correlationString(avgJanToMayInputArrayList, avgONDIndexArrayList)+"\n"+
				"Jan-May JFMAM = "+correlationString(avgJanToMayInputArrayList, avgJFMAMIndexArrayList)+"\n"+
				"Jan-May ANNUAL = "+correlationString(avgJanToMayInputArrayList, avgANNUALIndexArrayList)+"\n"+
				"LAG 1:\n"+
				"Jan-May JFM = "+correlationString(avgJanToMayInputArrayList1, avgJFMIndexArrayList1)+"\n"+
				"Jan-May AMJ = "+correlationString(avgJanToMayInputArrayList1, avgAMJIndexArrayList1)+"\n"+
				"Jan-May JAS = "+correlationString(avgJanToMayInputArrayList1, avgJASIndexArrayList1)+"\n"+
				"Jan-May OND = "+correlationString(avgJanToMayInputArrayList1, avgONDIndexArrayList1)+"\n"+
				"Jan-May JFMAM = "+correlationString(avgJanToMayInputArrayList1, avgJFMAMIndexArrayList1)+"\n"+
				"Jan-May ANNUAL = "+correlationString(avgJanToMayInputArrayList1, avgANNUALIndexArrayList1);
		
		String rString = boundarieStrings.get(0)+"/"+boundarieStrings.get(1)+" - "+boundarieStrings.get(2)+"/"+boundarieStrings.get(3)+"\t"+
				equivalentInputArrayList.size()+"\t"+
				correlationString(avgJanToMayInputArrayList, avgJFMIndexArrayList)+"\t"+
				correlationString(avgJanToMayInputArrayList, avgAMJIndexArrayList)+"\t"+
				correlationString(avgJanToMayInputArrayList, avgJASIndexArrayList)+"\t"+
				correlationString(avgJanToMayInputArrayList, avgONDIndexArrayList)+"\t"+
				correlationString(avgJanToMayInputArrayList, avgJFMAMIndexArrayList)+"\t"+
				correlationString(avgJanToMayInputArrayList, avgANNUALIndexArrayList)+"\t"+
				correlationString(avgJanToMayInputArrayList1, avgJFMIndexArrayList1)+"\t"+
				correlationString(avgJanToMayInputArrayList1, avgAMJIndexArrayList1)+"\t"+
				correlationString(avgJanToMayInputArrayList1, avgJASIndexArrayList1)+"\t"+
				correlationString(avgJanToMayInputArrayList1, avgONDIndexArrayList1)+"\t"+
				correlationString(avgJanToMayInputArrayList1, avgJFMAMIndexArrayList1)+"\t"+
				correlationString(avgJanToMayInputArrayList1, avgANNUALIndexArrayList1);

		System.out.println(cString);
		
		return rString;
	}
	
	public boolean isOkANA (ArrayList<String> boundarieStrings, String date) {
		boolean ans=false;
		int yearBegin=0, monthBegin=0, yearEnd=0, monthEnd=0;
		int lineYear=0, lineMonth=0;
		
		yearBegin = Integer.parseInt(boundarieStrings.get(0));
		monthBegin = Integer.parseInt(boundarieStrings.get(1));
		yearEnd = Integer.parseInt(boundarieStrings.get(2));
		monthEnd = Integer.parseInt(boundarieStrings.get(3));
		
		lineYear = Integer.parseInt(date.substring(6, 10));
		lineMonth = Integer.parseInt(date.substring(3, 5));
		
		if(lineYear<yearEnd & lineYear>yearBegin) {
			ans=true;
		} else {
			if(lineYear==yearBegin) {
				if(lineMonth>=monthBegin) {
					ans=true;
				}
			} else {
				if(lineYear==yearEnd) {
					if(lineMonth<=monthEnd) {
						ans=true;
					}
				}
			}
		}
		
		return ans;
	}
	
	public boolean isOkNOAA (ArrayList<String> boundarieStrings, String date) {
		boolean ans=false;
		int yearBegin=0, monthBegin=0, yearEnd=0, monthEnd=0;
		int lineYear=0, lineMonth=0;

		yearBegin = Integer.parseInt(boundarieStrings.get(0));
		monthBegin = Integer.parseInt(boundarieStrings.get(1));
		yearEnd = Integer.parseInt(boundarieStrings.get(2));
		monthEnd = Integer.parseInt(boundarieStrings.get(3));
		
		
		if(date.length()==6) {
			lineYear = Integer.parseInt(date.substring(0, 4));
			lineMonth = Integer.parseInt(date.substring(5, 6));
		} else {
			if(date.length()==7) {
				lineYear = Integer.parseInt(date.substring(0, 4));
				lineMonth = Integer.parseInt(date.substring(5, 7));
			}
		}
		
		if(lineYear<yearEnd & lineYear>yearBegin) {
			ans=true;
		} else {
			if(lineYear==yearBegin) {
				if(lineMonth>=monthBegin) {
					ans=true;
				} 
			} else {
				if(lineYear==yearEnd) {
					if(lineMonth<=monthEnd) {
						ans=true;
					}
				}
			}
		}
		
		return ans;
	}
	
	//TODO <<FOR DEBUG PURPOSES>>
	/*
	public int findLineANA (int year, int month, ArrayList<String> dataArrayList) {
		int size = dataArrayList.size();
		int mark = 0;
		
		//TODO REMOVER
		//System.out.println("begin");
		//System.out.println(year);
		//System.out.println(month);
		//System.out.println(dataArrayList.get(1));
		
		for (int i=0; i<(size/2); i++) {
			if(Integer.parseInt(dataArrayList.get(2*i).substring(6, 10))==year
					&
					Integer.parseInt(dataArrayList.get(2*i).substring(3, 5))==month) {
				mark=2*i;
			}
		}
		return mark;
		
	}
	
	public int findLineNOAA (int year, int month, ArrayList<String> dataArrayList) {
		int size = dataArrayList.size();
		int mark = 0;
		
		for (int i=0; i<(size/2); i++) {
			if(dataArrayList.get(2*i).length()==6) {
				if(Integer.parseInt(dataArrayList.get(2*i).substring(0, 4))==year
						&
						Integer.parseInt(dataArrayList.get(2*i).substring(5, 6))==month) {
					mark=2*i;
				}
			} else {
				if (dataArrayList.get(2*i).length()==7) {
					if(Integer.parseInt(dataArrayList.get(2*i).substring(0, 4))==year
							&
							Integer.parseInt(dataArrayList.get(2*i).substring(5, 7))==month) {
						mark=2*i;
					}
				}
			}
		}
		return mark;
	}
	*/
	
	public String correlationString (ArrayList<Double> inputArrayList, ArrayList<Double> indexArrayList) {
		int n = inputArrayList.size();
		double inputSum=0, inputSqrSum=0, indexSum=0, indexSqrSum=0, prodSum=0;
		double r;
		
		//TODO REMOVER
		//System.out.println(inputArrayList.size());
		//System.out.println(indexArrayList.size());
		
		for (int i=0; i<n; i++) {
			inputSum = inputSum + inputArrayList.get(i);
			indexSum = indexSum + indexArrayList.get(i);
			inputSqrSum = inputSqrSum + Math.pow(inputArrayList.get(i),2);
			indexSqrSum = indexSqrSum + Math.pow(indexArrayList.get(i),2);
			prodSum = prodSum + inputArrayList.get(i) * indexArrayList.get(i);
		}
		
		r = (n*prodSum-inputSum*indexSum)/
				(Math.sqrt(
						(n*inputSqrSum-Math.pow(inputSum, 2))*
						(n*indexSqrSum-Math.pow(indexSum, 2))));
		
		String ansString = String.valueOf(r);
		return ansString;		
	}
	
	public void callAnalysis () throws Exception{
		String inputFileString = System.getProperty("user.dir")+"\\src\\files\\input.txt";
		File inputFile = new File(inputFileString);
		Scanner ifScanner = new Scanner(inputFile);
		ifScanner.useDelimiter("\t|\r\n");
		ArrayList<String> inputDataArrayList = new ArrayList<>();

		while (ifScanner.hasNext()) {
			String tempString = ifScanner.next();
			inputDataArrayList.add(tempString);
		}
		ifScanner.close();

		checkInputData(inputDataArrayList);

		ArrayList<String> iURLList = populateiURL();
		int nFiles = iURLList.size()/2;
		
		File outputFile = new File(System.getProperty("user.dir")+"\\src\\files\\output.txt");
		PrintWriter writer = new PrintWriter(outputFile);
		
		
		for (int i=0; i<nFiles; i++) {
			String indexPathString = System.getProperty("user.dir")+"\\src\\files\\"+iURLList.get(2*i)+".txt";
			System.out.println(iURLList.get(2*i)+":  started!");
			String rString = performAnalysis(inputDataArrayList, indexPathString);
			writer.print(iURLList.get(2*i)+"\t");
			writer.print(rString+"\n");
			System.out.println(iURLList.get(2*i)+":  done!\n");
		}
		
		writer.close();
	}
	
	@SuppressWarnings("unused")
	public void exportInputDataForNoaa() throws Exception{
		File file = new File(System.getProperty("user.dir")+"\\src\\files\\input.txt");
		Scanner scanner = new Scanner(file);
		scanner.useDelimiter("\t|\r\n");
		
		ArrayList<String> inputDatArrayList = new ArrayList<>();
		
		while(scanner.hasNext()) {
			inputDatArrayList.add(scanner.next());
		}
		scanner.close();
		
		System.out.println("There was/were "+inputDatArrayList.size()+" entries!");
		checkInputData(inputDatArrayList);
		
		int size = inputDatArrayList.size();
		String initialDateString = inputDatArrayList.get(0);
		String finaldateString = inputDatArrayList.get(size-2);
		
		String initialYearString = initialDateString.substring(6,10);
		String initialMonthString = initialDateString.substring(3,5);
		String finalYearString = finaldateString.substring(6,10);
		String finalMonthString = finaldateString.substring(3,5);
		
		File inputNoaaFile = new File(System.getProperty("user.dir")+"\\src\\files\\inputNoaa.txt");
		PrintWriter printer = new PrintWriter(inputNoaaFile);
		
		printer.print(initialYearString+" "+(Integer.parseInt(finalYearString)-1)+"\n");
		
		int mark=1;
		
		for(int i=Integer.parseInt(initialYearString); i<Integer.parseInt(finalYearString); i++) {
			printer.print(i+" ");
			for (int j=0; j<12; j++) {
				if (i==Integer.parseInt(finalYearString)) {
					//printer.print(inputDatArrayList.get(mark));
					//mark=mark+2;
				} else {
					printer.print(inputDatArrayList.get(mark)+" ");
					mark=mark+2;
				}
			}
			printer.print("\n");
		}
		
		printer.close();
	}
	
	@SuppressWarnings("unused")
	public void exportInputDataArrayListForCorrelation () throws Exception{
		
		File inputFile = new File(System.getProperty("user.dir")+"\\src\\files\\input.txt");
		
		Scanner scanner = new Scanner(inputFile);
		ArrayList<String> inputDataArrayList = new ArrayList<>();
		
		while (scanner.hasNext()) {
			inputDataArrayList.add(scanner.next());
		}
		scanner.close();
		
		int inputSize = inputDataArrayList.size();

		String inputBeginDateString = inputDataArrayList.get(0);
		String inputEndDateString = inputDataArrayList.get(inputSize-2);

		//TODO REMOVE
		//System.out.println(inputBeginDateString);
		//System.out.println(inputEndDateString);
		//System.out.println(indexBeginDateString);
		//System.out.println(indexEndDateString);

		int inputMonthBegin=0, inputYearBegin=0, inputMonthEnd=0, inputYearEnd=0;
		
		inputMonthBegin = Integer.parseInt(inputBeginDateString.substring(3, 5));
		inputYearBegin = Integer.parseInt(inputBeginDateString.substring(6, 10));
		inputMonthEnd = Integer.parseInt(inputEndDateString.substring(3, 5));
		inputYearEnd = Integer.parseInt(inputEndDateString.substring(6, 10));
		
		ArrayList<String> serialDataArrayList = new ArrayList<String>();
		
		for (int i=0; i<(inputSize/2); i++) {
			serialDataArrayList.add(inputDataArrayList.get(2*i+1));
		}
		
		File outputFile = new File(System.getProperty("user.dir")+"\\src\\files\\inputRegressionForPCA.txt");
		
		PrintWriter printer = new PrintWriter(outputFile);
		
		printer.print(inputYearBegin+" "+inputYearEnd+"\n");
		int mark=0;
		for (int i=inputYearBegin; i<(inputYearEnd+1); i++) {
			printer.print(i+" ");
			for (int j=0; j<12; j++) {
				if(i==inputYearEnd) {
					if ((j+1)<=inputMonthEnd) {
						printer.print(serialDataArrayList.get(mark)+" ");
						mark++;
					}
				} else {
					printer.print(serialDataArrayList.get(mark)+" ");
					mark++;
				}
			}
			printer.print("\n");
		}
		printer.close();
		System.out.println("mark");
	}
	
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		ClimaticIndexes cli = new ClimaticIndexes();
		cli.importIndexData();
		cli.callAnalysis();
		cli.exportInputDataForNoaa();
		cli.exportInputDataArrayListForCorrelation();
		System.out.println("Done!");
	}
}