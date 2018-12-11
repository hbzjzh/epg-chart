package cn.paput.epgchart.maintain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MediaInfoCorrect {
    @Autowired
    JdbcTemplate jdbcTemplate;

    static String SQL_GET_MEDIA_INFO = " select ID,MEDIA_CODE,MEDIA_NAME,SERIESFLAG,SP_CODE,ORDER_FLAG,EPISODES,SERIES_PARENT,SERIES_NUM from media_info where type='vod_playing' and SERIESFLAG = ? order by ID desc limit ? ";

    private void getMediaInfoFromDB(String flag, int limit) {
        List<MediaInfoVO> mediaList = jdbcTemplate.query(SQL_GET_MEDIA_INFO, new Object[]{flag, limit}, new BeanPropertyRowMapper<>(MediaInfoVO.class));
        System.out.print(mediaList);
    }

    @RequestMapping("/correct/{flag}/{limit}")
    public void correct(@PathVariable("flag") String flag, @PathVariable("limit") int limit){
        getMediaInfoFromDB(flag, limit);
    }

    public static class MediaInfoVO {
        private String id;
        private String mediaCode;
        private String mediaName;
        private String seriesFlag;
        private String spCode;
        private String orderFlag;
        private String episodes;
        private String seriesParent;
        private String seriesNum;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMediaCode() {
            return mediaCode;
        }

        public void setMediaCode(String mediaCode) {
            this.mediaCode = mediaCode;
        }

        public String getMediaName() {
            return mediaName;
        }

        public void setMediaName(String mediaName) {
            this.mediaName = mediaName;
        }

        public String getSeriesFlag() {
            return seriesFlag;
        }

        public void setSeriesFlag(String seriesFlag) {
            this.seriesFlag = seriesFlag;
        }

        public String getSpCode() {
            return spCode;
        }

        public void setSpCode(String spCode) {
            this.spCode = spCode;
        }

        public String getOrderFlag() {
            return orderFlag;
        }

        public void setOrderFlag(String orderFlag) {
            this.orderFlag = orderFlag;
        }

        public String getEpisodes() {
            return episodes;
        }

        public void setEpisodes(String episodes) {
            this.episodes = episodes;
        }

        public String getSeriesParent() {
            return seriesParent;
        }

        public void setSeriesParent(String seriesParent) {
            this.seriesParent = seriesParent;
        }

        public String getSeriesNum() {
            return seriesNum;
        }

        public void setSeriesNum(String seriesNum) {
            this.seriesNum = seriesNum;
        }

        @Override
        public String toString() {
            return "MediaInfoVO{" +
                    "id='" + id + '\'' +
                    ", mediaCode='" + mediaCode + '\'' +
                    ", mediaName='" + mediaName + '\'' +
                    ", seriesFlag='" + seriesFlag + '\'' +
                    ", spCode='" + spCode + '\'' +
                    ", orderFlag='" + orderFlag + '\'' +
                    ", episodes='" + episodes + '\'' +
                    ", seriesParent='" + seriesParent + '\'' +
                    ", seriesNum='" + seriesNum + '\'' +
                    '}';
        }
    }

}
