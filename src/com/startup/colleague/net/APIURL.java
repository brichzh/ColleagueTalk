package com.startup.colleague.net;

/**
 * 
 * @author yongzhang
status错误码
100001 无效cookie
100002 用户名不存在
100003 密码错误
100005 内容为空
100006 该邮箱已经被注册
100007 不合法链接
100008 用户名存在
100009 邮箱未激活

1 暂时不对该公司开放
 *
 */
public class APIURL {
  private final static String HOST = "http://aidi.me/shuo";
  public  final static String ACCOUNT = HOST + "/account.php";
  public  final static String NEWHOST = "http://123.57.228.227";
  
  public  final static String NOTE = HOST + "/note.php";
  public  final static String NEWROOT = NEWHOST + "/index.php";
  public  final static String TOPIC_COMMENT = NEWHOST + "/index.php?c=topic&a=comment";
  public  final static String POST_TOPIC = NEWHOST + "/index.php?c=topic&a=postTopic";
  public  final static String POST_REGISTER = NEWHOST + "/index.php?c=account&a=register";
  public  final static String POST_LOGIN = NEWHOST + "/index.php?c=account&a=login";
}
