<template>
    <div class="download-section">
      <!-- 统计展示区 -->
      <!-- <div class="download-stats">
        <p>本页面发起的总下载量：{{ totalDownloads }} 次</p>
      </div> -->
  
      <!-- 下载按钮区 -->
      <div class="download-buttons">
        <button @click="handleDownload(0, 'https://www.123865.com/s/2f38Vv-D6CzH')" class="download-button pan-button">
          <div class="button-content">
            <img src="/123pan.png" alt="123云盘" class="button-icon">
            <div class="button-text">
              <div class="primary-text">网盘下载（推荐）</div>
              <div class="secondary-text">(123云盘)</div>
            </div>
          </div>
        </button>
  
        <button @click="handleDownload(1, 'https://github.com/murangogo/MoeTranslate/releases/latest')" class="download-button github-button">
          <div class="button-content">
            <img src="/github.png" alt="GitHub" class="button-icon">
            <div class="button-text">
              <div class="primary-text">Github下载</div>
              <div class="secondary-text">(Release)</div>
            </div>
          </div>
        </button>
  
        <button @click="handleDownload(2, 'https://repo.azuki.top/MoeTranslate_arm64-v8a_5.2.1.apk')" class="download-button local-button">
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
  export default {
    name: 'DownloadButtons3',
    data() {
      return {
        totalDownloads: 0
      }
    },
    mounted() {
      // 页面加载时获取总下载次数
      // this.getTotalDownloadCount();
    },
    methods: {
      async getTotalDownloadCount() {
        // try {
        //   const response = await fetch('https://cfapi.moetranslate.top/api/getTotalDownloadCount');
        //   const data = await response.json();
        //   this.totalDownloads = data.total || 0;
        // } catch (error) {
        //   console.error('获取总下载次数失败:', error);
        // }
      },
      async handleDownload(type, url) {
        // 触发下载
        window.open(url, '_blank');

        try {
          // 更新下载统计
          const response = await fetch(`https://cfapi.moetranslate.top/api/updateDownload?type=${type}`, {
            method: 'POST',
          });
  
          if (response.ok) {
            // 更新总下载次数
            // await this.getTotalDownloadCount();
          }
        } catch (error) {
          console.error('更新下载统计失败:', error);
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
  