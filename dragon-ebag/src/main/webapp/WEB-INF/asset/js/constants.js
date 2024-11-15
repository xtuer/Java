// URL 都是常量，不要修改

// 文件系统的 URL
FileSystemUrls = {
    FILE_SYSTEM_FILE_UPLOAD_COMMON:  '/filesystem/file-upload-common/{directoryId}',                   // 上传文件的 URL
    REST_FILE_SYSTEM_FILES_WITH_ID:  '/rest/filesystem/files/{fileId}',                           // 访问文件的 URL
    REST_FILE_SYSTEM_FILES:          '/rest/filesystem/directories/{directoryId}/files',          // 访问目录下的文件和子文件夹的 URL
    REST_FILE_SYSTEM_DIRECTORIES:    '/rest/filesystem/directories',                              // 访问目录的 URL
    REST_FILE_SYSTEM_SUBDIRECTORIES: '/rest/filesystem/directories/{directoryId}/subdirectories', // 访问目录下子文件夹的 URL
    REST_FILE_SYSTEM_FILES_WITHOUT_SUBDIRECTORIES: '/rest/filesystem/directories/{directoryId}/files-without-subdirectories', // 访问目录下的文件的 URL
};

