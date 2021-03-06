package cn.paput.epgchart.maintain;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @author sean
 */
@RestController
public class MediaInfoCorrect {
    @Autowired
    JdbcTemplate jdbcTemplate;

    private final static String SQL_GET_MEDIA_INFO = " select ID,MEDIA_CODE,MEDIA_NAME,SERIESFLAG,SP_CODE,ORDER_FLAG,EPISODES,SERIES_PARENT,SERIES_NUM from media_info where type='vod_playing' and SERIESFLAG = ? order by ID desc limit ? ";
    private final static String SQL_UPDATE_MEDIA_INFO_BY_ID = " update media_info set MEDIA_NAME=?,SERIESFLAG=?,SP_CODE=?,ORDER_FLAG=?,EPISODES=?,SERIES_PARENT=?,SERIES_NUM=? where type='vod_playing' and ID=?";
    private final static String SQL_UPDATE_MEDIA_INFO_BY_CODE = " update media_info set MEDIA_NAME=?,SERIESFLAG=?,SP_CODE=?,ORDER_FLAG=?,EPISODES=?,SERIES_PARENT=?,SERIES_NUM=? where type='vod_playing' and MEDIA_CODE=?";
    private final static String URL_GET_PROGRAM_INFO = "http://182.245.29.104:78/GetProgramInfo?programId=";
    private final static boolean UPDATE_SWITCH = true;

    private final static String HTTP_OK = "200";
		private final static String RESULT = "result";
    private final static String NAGETIVE = "-1";
    private final static String ZERO = "0";
    private final static String 电影 = "0";
    private final static String 电视剧 = "1";
    private final static String 子剧集 = "2";

    private final static String 节目ID = "programId";
    private final static String 节目名称 = "programName";
    private final static String 节目类型 = "programType";
    private final static String 供应商 = "spCode";
    private final static String 是否付费 = "orderFlag";
    private final static String 剧集列表 = "programSub";
    private final static String 子剧集序号 = "programNumber";


    @RequestMapping("/correct/{flag}-{limit}")
    public void correct(@PathVariable(name="flag", required=false) String flag, @PathVariable("limit") int limit){
				List<MediaInfoVO> mediaList = jdbcTemplate.query(SQL_GET_MEDIA_INFO, new Object[]{flag, limit}, new BeanPropertyRowMapper<>(MediaInfoVO.class));
				int m = mediaList.size();
        StringBuffer sb;
        boolean doUpdate;
        JSONObject info;
        for(MediaInfoVO voOld : mediaList) {
						System.out.println(m + "、" + voOld.toString());
						sb = new StringBuffer();
						info = getJsonFromGS(voOld.mediaCode);
            if(info==null) {
                voOld.seriesFlag = NAGETIVE;
                updateMediaInfo(voOld, "无数据", sb);
            }
            doUpdate = false;
            doUpdate = ifUpdate(doUpdate, sb, "节目名称", voOld.mediaName, info.getString(节目名称));
            voOld.mediaName = info.getString(节目名称);
            doUpdate = ifUpdate(doUpdate, sb, "节目类型", voOld.seriesFlag, info.getString(节目类型));
            voOld.seriesFlag = info.getString(节目类型);
            doUpdate = ifUpdate(doUpdate, sb, "---------供应商", voOld.spCode, info.getString(供应商));
            voOld.spCode = info.getString(供应商);
            doUpdate = ifUpdate(doUpdate, sb, "是否付费", voOld.orderFlag, info.getString(是否付费));
            voOld.orderFlag = info.getString(是否付费);
            if(电影.equals(voOld.seriesFlag)) {
                voOld.episodes = ZERO;
                voOld.seriesParent = ZERO;
                voOld.seriesNum = null;
                if(doUpdate) {
                    updateMediaInfo(voOld, "电影", sb);
                }
            } else if(电视剧.equals(voOld.seriesFlag)) {
                JSONArray sub = info.getJSONArray(剧集列表);
                doUpdate = ifUpdate(doUpdate, sb, "剧集列表", voOld.episodes, String.valueOf(sub.size()));
                voOld.episodes = String.valueOf(sub.size());
                voOld.seriesParent = ZERO;
                voOld.seriesNum = null;
                if(doUpdate) {
                    updateMediaInfo(voOld, "电视剧", sb);
                    JSONObject item = null;
                    for(int j=0;j<sub.size();j++) {
                        item = sub.getJSONObject(j);
                        voOld.mediaCode = item.getString(节目ID);
                        voOld.mediaName = item.getString(节目名称);
                        voOld.seriesFlag = item.getString(节目类型);
                        voOld.seriesParent = voOld.id;
                        voOld.seriesNum = item.getString(子剧集序号);
                        updateMediaInfo(voOld, "子剧集", sb);
                    }
                }
            } else if(子剧集.equals(voOld.seriesFlag)) {
                if(doUpdate) {
                    updateMediaInfo(voOld, "子剧集", sb);
                }
            } else {
                System.out.println("无类型：" + voOld.mediaCode);
            }
            m--;
				}
    }

    private void updateMediaInfo(MediaInfoVO vo, String memo, StringBuffer sb) {
        int ar = 0;
        if(子剧集.equals(vo.seriesFlag)) {
            sb = new StringBuffer(memo + "《" +vo.mediaName + "》");
            if(UPDATE_SWITCH) {
                ar = jdbcTemplate.update(SQL_UPDATE_MEDIA_INFO_BY_CODE, new Object[] {vo.mediaName, vo.seriesFlag, vo.spCode, vo.orderFlag, vo.episodes, vo.seriesParent, vo.seriesNum, vo.mediaCode});
            }
        } else {
            sb.insert(0, memo + "《" +vo.mediaName + "》");
            if(UPDATE_SWITCH) {
                ar = jdbcTemplate.update(SQL_UPDATE_MEDIA_INFO_BY_ID, new Object[] {vo.mediaName, vo.seriesFlag, vo.spCode, vo.orderFlag, vo.episodes, vo.seriesParent, vo.seriesNum, vo.id});
            }
        }
        System.out.println(sb.toString() + ", 影响:" + ar);
    }

    private JSONObject getJsonFromGS(String mediaCode) {
        RestTemplate restTemplate = new RestTemplate();
        String json = restTemplate.getForObject(URL_GET_PROGRAM_INFO + mediaCode, String.class);
        JSONObject info = JSONObject.parseObject(json);
        if(info!=null && HTTP_OK.equals(info.getString(RESULT))) {
            return info;
        }
        return null;
    }

    private boolean ifUpdate(boolean doUpdate, StringBuffer sb, String column, String arg1, String arg2) {
        if(arg1==null) {
            doUpdate = true;
            sb.append(", " + column+"[null," + arg2 + "]");
        } else if(arg2==null)  {
            doUpdate = true;
            sb.append(", " + column+"[" + arg1 + ",null]");
        } else if(!arg1.equals(arg2)) {
            doUpdate = true;
            sb.append(", " + column + "[" + arg1 + "," + arg2 + "]");
        }
        return doUpdate;
    }

    public static class MediaInfoVO {
        @Getter
        @Setter
        private String id;
        @Getter
        @Setter
        private String mediaCode;
        @Getter
        @Setter
        private String mediaName;
        @Getter
        @Setter
        private String seriesFlag;
        @Getter
        @Setter
        private String spCode;
        @Getter
        @Setter
        private String orderFlag;
        @Getter
        @Setter
        private String episodes;
        @Getter
        @Setter
        private String seriesParent;
        @Getter
        @Setter
        private String seriesNum;
        @Getter
        @Setter
        private JSONArray subList;

        public MediaInfoVO() {

        }

        public MediaInfoVO(String id, String mediaCode, String mediaName, String seriesFlag, String spCode, String orderFlag, String episodes, String seriesParent, String seriesNum, JSONArray subList) {
            this.id = id;
            this.mediaCode = mediaCode;
            this.mediaName = mediaName;
            this.seriesFlag = seriesFlag;
            this.spCode = spCode;
            this.orderFlag = orderFlag;
            this.episodes = episodes;
            this.seriesParent = seriesParent;
            this.seriesNum = seriesNum;
            this.subList = subList;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("{");
            sb.append("\"id\":\"").append(id).append('\"');
            sb.append(",\"mediaCode\":\"").append(mediaCode).append('\"');
            sb.append(",\"mediaName\":\"").append(mediaName).append('\"');
            sb.append(",\"seriesFlag\":\"").append(seriesFlag).append('\"');
            sb.append(",\"spCode\":\"").append(spCode).append('\"');
            sb.append(",\"orderFlag\":\"").append(orderFlag).append('\"');
            sb.append(",\"episodes\":\"").append(episodes).append('\"');
            sb.append(",\"seriesParent\":\"").append(seriesParent).append('\"');
            sb.append(",\"seriesNum\":\"").append(seriesNum).append('\"');
            sb.append('}');
            return sb.toString();
        }

    }

}
