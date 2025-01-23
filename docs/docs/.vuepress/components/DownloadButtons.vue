<template>
  <div class="download-section">
    <!-- 统计展示区 -->
    <div class="download-stats">
      <p>本页面发起的总下载量：{{ totalDownloads }} 次</p>
    </div>

    <!-- 下载按钮区 -->
    <div class="download-buttons">
      <button @click="handleDownload('cloud_disk', 'https://www.123865.com/s/2f38Vv-orszH')" class="download-button pan-button">
        <div class="button-content">
          <img src="/123pan.png" alt="123云盘" class="button-icon">
          <div class="button-text">
            <div class="primary-text">网盘下载</div>
            <div class="secondary-text">(123云盘)</div>
          </div>
        </div>
      </button>

      <button @click="handleDownload('github', 'https://github.com/murangogo/MoeTranslate/releases/latest')" class="download-button github-button">
        <div class="button-content">
          <img src="/github.png" alt="GitHub" class="button-icon">
          <div class="button-text">
            <div class="primary-text">Github下载</div>
            <div class="secondary-text">(Release)</div>
          </div>
        </div>
      </button>

      <button @click="handleDownload('local', 'https://repo.azuki.top/MoeTranslate_5.0.0.apk')" class="download-button local-button">
        <div class="button-content">
          <img src="/cloudflare.png" alt="CloudFlare" class="button-icon">
          <div class="button-text">
            <div class="primary-text">本地下载</div>
            <div class="secondary-text">(CloudFlare R2)</div>
          </div>
        </div>
      </button>
    </div>
  </div>
</template>

<script>
import AV from 'leancloud-storage'

export default {
  name: 'DownloadButtons',
  data() {
    return {
      localDownloads: 0,
      githubDownloads: 0,
      cloudDiskDownloads: 0,
      totalDownloads: 0
    }
  },
  mounted() {

    // 初始化 LeanCloud
    AV.init({
      appId: VITE_LEANCLOUD_APP_ID,
      appKey: VITE_LEANCLOUD_APP_KEY,
      serverURL: VITE_LEANCLOUD_SERVER_URL
    })
    
    // 获取所有下载统计
    this.getDownloadCounts()
  },
  methods: {
    async getDownloadCounts() {
      try {
        const query = new AV.Query('Downloads')
        const download = await query.first()
        
        if (download) {
          this.localDownloads = download.get('local_downloads') || 0
          this.githubDownloads = download.get('github_downloads') || 0
          this.cloudDiskDownloads = download.get('cloud_disk_downloads') || 0
          this.totalDownloads = this.localDownloads + this.githubDownloads + this.cloudDiskDownloads
        }
      } catch (error) {
        console.error('获取下载统计失败:', error)
      }
    },
    async handleDownload(type, url) {
      try {
        // 更新下载统计
        const query = new AV.Query('Downloads')
        let download = await query.first()
        
        if (!download) {
          // 如果记录不存在，创建新记录
          const Downloads = AV.Object.extend('Downloads')
          download = new Downloads()
        }
        
        // 根据下载类型更新对应计数
        const field = `${type}_downloads`
        download.increment(field)
        await download.save()
        
        // 更新本地显示
        await this.getDownloadCounts()
        
        // 触发下载
        window.open(url, '_blank')
      } catch (error) {
        console.error('更新下载统计失败:', error)
      }
    }
  }
}
</script>

<style scoped>
.download-section {
  margin: 2rem 0;
}

.download-stats {
  margin-bottom: 1.5rem;
  padding: 1rem;
  background-color: #f5f5f5;
  border-radius: 8px;
}

.download-buttons {
  display: flex;
  gap: 2rem;
  flex-wrap: wrap;
  justify-content: center;
}

.download-button {
  min-width: 200px;
  padding: 1rem 1.5rem;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.download-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0);
  transition: background-color 0.3s ease;
}

.download-button:hover::before {
  background-color: rgba(255, 255, 255, 0.3);
}

.button-content {
  display: flex;
  align-items: center;
  gap: 1rem;
  position: relative;
  z-index: 1;
}

.button-icon {
  width: 35px;
  height: 35px;
  object-fit: contain;
}

.button-text {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  color: white;
}

.primary-text {
  font-size: 1rem;
  font-weight: 500;
}

.secondary-text {
  font-size: 0.875rem;
  opacity: 0.8;
}

.pan-button {
  background-color: #3A7CF7;
}

.github-button {
  background-color: #1F2328;
}

.local-button {
  background-color: #F6821F;
}
</style>