const CHUNK_SIZE = 1024 * 1024 * 20; // 20MB 分片大小
let currentChunk = 0; // 将 currentChunk 移到外部作用域

function uploadFile() {
    const fileInput = document.getElementById('fileInput');
    const file = fileInput.files[0];

    if (!file) {
        alert('请选择一个文件');
        return;
    }

    const totalChunks = Math.ceil(file.size / CHUNK_SIZE);
    let currentChunk = 0;

    function uploadNextChunk() {
        const start = currentChunk * CHUNK_SIZE;
        const end = Math.min((currentChunk + 1) * CHUNK_SIZE, file.size);
        const chunk = file.slice(start, end);

        // 获取 MD5
        getMd(chunk)
            .then(md5 => {
                const formData = new FormData();
                formData.append('file', chunk);
                formData.append('currentChunk', currentChunk);
                formData.append('totalChunks', totalChunks);
                formData.append('originalFilename', file.name);
                formData.append('md5', md5);

                const xhr = new XMLHttpRequest();
                xhr.open('POST', 'http://localhost:8081/upload', true);
                xhr.onload = function () {
                    if (xhr.status === 200) {
                        currentChunk++;
                        if (currentChunk < totalChunks) {
                            uploadNextChunk();
                        } else {
                            alert('上传完成');
                        }
                    } else {
                        alert('上传失败');
                    }
                };

                xhr.send(formData);
            })
            .catch(error => {
                // 处理错误
                console.error(error.message);
                alert('获取MD5失败');
            });
    }

    uploadNextChunk();
}


function getMd(file) {
    const fileReader = new FileReader()
    fileReader.readAsBinaryString(file);
    const md5 = '';
    fileReader.onload = e => {
        md5 = SparkMD5.hashBinary(e.target.result);
        console.log(md5);
    }
    return md5;
}

function getMd(file) {
    return new Promise((resolve, reject) => {
        const fileReader = new FileReader();

        fileReader.onload = function (e) {
            const md5 = SparkMD5.hashBinary(e.target.result);
            console.log(md5);
            resolve(md5);
        };

        fileReader.onerror = function (e) {
            reject(new Error('File read error.'));
        };

        fileReader.readAsBinaryString(file);
    });
}


// ...

