package com.wecombo.ml.irecog.util;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NLPUtil {
    public static String getIDCardInfo(String content){


        content = preProcessing(content);
        return "姓名："+getName(content) +"\n" +"性别："+getGender(content) +"\n" +"民族："+getNationality(content)+
                "\n" +"出生："+getBirth(content) +"\n"+"住址："+getAddr(content) + "\n"+"身份证号码："+getID(content.split(" "))+"\n";
    }

    /**
     * 文本预处理，删去符号，分隔符整齐化
     * @param content
     * @return
     */
    private static String preProcessing(String content){
        //去除杂乱字符，剩下字母、数字、中文
        content = content.replaceAll("[^A-Za-z0-9|^\u4E00-\u9FA5]", " ");
        //结构统一，每个部分用空格隔开
        content = content.replaceAll("\n"," ").replaceAll(" {1,9}", " ");
        //如果类别标注信息完整，将其格式化
        content = content.replaceAll("姓 名", "姓名").replaceAll("性 别", "性别").replaceAll("民 族", "民族")
                .replaceAll("出 生","出生").replaceAll("住 址", "住址");

        return content;
    }

    /**
     * 先根据标签提取，失败则根据性别标签提取，再失败则根据性别词提取，
     * 再失败则根据前几个词提取（此时已经识别的不好了，大概差不多就行）
     * @param content
     * @return
     */
    private static String getName(String content){
        try{
            if(content.contains("姓名")){
                if(content.contains("性别")){
                    String temp = getString(content,"姓名 .{1,15} 性别");
                    return temp.substring(3, temp.length()-3).replaceAll(" ", "");
                }
                else{
                    String gender = getGender(content);
                    String temp = getString(content,"姓名 .{1,15} "+gender);
                    return temp.substring(3, temp.length()-3).replaceAll(" ", "");
                }
            }
            else{
                String gender = getGender(content);
                String temp = getString(content,".{1,15} "+gender);
                return temp.substring(0, temp.length()-2).replaceAll(" ", "");
            }
        }catch(Exception e){
            return null;
        }
    }

    /**
     * 这个必须识别出来，可以加几个性别的形近字
     * @param content
     * @return
     */
    private static String getGender(String content){
        if(content.contains("女"))
            return "女";
        else
            return "男";
    }

    /**
     * 先根据标签提取，再根据后标签提取，
     * 都模糊时先跑字典，
     * 再根据个别标签内容提取，
     * 最难时根据性别或者出生年提取（此时已经很差了，差不多就行）
     * @param content
     * @return
     */
    private static String getNationality(String content){
        try{
            if(content.substring(0, content.length()/2).contains("汉") ||
                    content.substring(0, content.length()/2).contains("又"))
                return "汉";
            if(content.contains("民族")){
                if(content.contains("出生")){
                    String temp = getString(content,"民族 .{1,9} 出生");
                    return temp.substring(3, temp.length()-3).replaceAll(" ", "");
                }
                else{
                    String temp = getString(content,"民族 .{1,5} ");
                    return temp.substring(3, temp.length()-1).replaceAll(" ", "");
                }
            }
            else{
                String gender = getGender(content);
                String temp = getString(content,gender+" .{1,5} ");
                return temp.substring(2, temp.length()-1).replaceAll(" ", "");
            }
        }catch(Exception e){
            return null;
        }
    }

    /**
     * 先根据标签提取，再根据后标签提取
     * 不成功时，根据地址关键字提取
     * 再失败根据不完整标签提取
     * @param content
     * @return
     */
    private static String getAddr(String content){
        try{
            String res = "";
            String[] contentList = content.split(" ");
            for(int i = 0;i < contentList.length;i++){
                if(contentList[i].contains("省")|| contentList[i].contains("巷") ||contentList[i].contains("路")
                        ||contentList[i].contains("号") || contentList[i].contains("街")||contentList[i].contains("道"))
                    res += contentList[i];
            }
            return res.replaceAll(" ", "");
        }catch(Exception e){
            return null;
        }
    }

    /**
     * 先尝试获取完整的身份证号码字段
     * 再尝试根据标签获取
     * 再尝试根据模糊标签获取
     * @param contentList
     * @return
     */
    private static String getID(String[] contentList){
        for(int i = 0;i < contentList.length;i++){
            if(contentList[i].matches("^\\w{10,17}[\\d|x|X]$"))
                return contentList[i].replaceAll("q", "9");
        }
        return null;
    }

    /**
     * 尝试根据标签获取
     * 再尝试根据模糊标签获取
     * @param content
     * @return
     */
    private static String getBirth(String content){
        try{
            content = content.replaceAll(" ", "");
            String date = "";
            if(content.contains("年") &&content.contains("月")&&content.contains("日")){
                date = getString(content, "\\d{4}年") + getString(content,"\\d{1,2}月") + getString(content,"\\d{1,2}日");
            }
            else{
                date = getString(content, "\\d{4}.{0,3}\\d{1,2}.{0,3}\\d{1,2}.");
            }

            //切开年和其他
            String year = date.substring(0,4);
            String monthDay = date.substring(4);
            if(monthDay.charAt(0) > '9' || monthDay.charAt(0) < '0')
                monthDay = monthDay.substring(1);
            if(monthDay.split("[^\\d]").length > 1){
                return year + "年"+monthDay.split("[^\\d]")[0]+"月"+monthDay.split("[^\\d]")[1]+"日";
            }
            else
                return year + "年"+monthDay+"日";
        }catch(Exception e){
            return null;
        }
    }

    /**
     * 获取查询的字符串
     * 将匹配的字符串取出
     */
    private static String getString(String str, String regx) {
        //1.将正在表达式封装成对象Patten 类来实现
        Pattern pattern = Pattern.compile(regx);
        //2.将字符串和正则表达式相关联
        Matcher matcher = pattern.matcher(str);
        //3.String 对象中的matches 方法就是通过这个Matcher和pattern来实现的。
        matcher.find();
        return matcher.group();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String[] testStrings = {"姓 名 张立赛\n\n性 别 男\n\n民 族汉\n\n出 生 1994 年 9 月 16日\n\n任 址 河北省藁城市南董镇信家\n\n\\\n\n}>吼羞\n\n(`亳\n\n\\`一 、",
                "姓名\n\n张\n\n单乙\n\n性别 男\n\n民 族汉\n\n稷`\n\n出 生1994年9月16日\n\n耍\n\n雇\n\n住 址 河墟′\n\n霆′′撤\n\n`r`\n\n'翼 /伽 . ” 一′\n\n鲤′′-熹『\n\n″′ 薯”\n\n'羲'焘\n\n霸\n\n谱'/",
                "攒 立 赛\n\n男\n\n/又\n\n1994\n\n9 闫 16曰\n\n′'、\n\n‖ 川 河北省薹城市南堇镇信家\n\n营村政通街86号\n\n公民身份号码\n\n130182199409164415\n\nh薯__【[_一一一_一一一一一_一一一′一",
                "张立赛\n\n__\n\n峰\n\n男\n\n民族汉\n\n′'、\n\n士 主\n\n1994 年9月16日\n\n住 垭 河北省藁城市南董镇信家\n\n营村政通街86号\n\n′ 公民身份号码 130182199499164415"};

        for(int i = 0;i < testStrings.length;i++){
            String testStr = preProcessing(testStrings[i]);
            System.out.println(testStr +"\n"+ getName(testStr) +"\t" +getGender(testStr) +"\t" +getAddr(testStr) +
                    "\t" +getNationality(testStr) +"\t"+getID(testStr.split(" ")) + "\t"+getBirth(testStr)+"\n");
        }
    }

}

