import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SidebarComponent } from '../layout/sidebar/sidebar.component';
import { DocumentService } from '../../services/document.service';
import { HttpEventType, HttpResponse } from '@angular/common/http';

@Component({
    selector: 'app-documents',
    standalone: true,
    imports: [CommonModule, FormsModule, SidebarComponent],
    templateUrl: './documents.component.html',
    styleUrl: './documents.component.css'
})
export class DocumentsComponent implements OnInit {
    selectedFiles?: FileList;
    currentFile?: File;
    progress = 0;
    message = '';
    fileInfos: any[] = [];

    constructor(private documentService: DocumentService) { }

    ngOnInit(): void {
        this.fetchFiles();
    }

    selectFile(event: any): void {
        this.selectedFiles = event.target.files;
    }

    activeTab = 'documents';
    importType = 'transactions';
    importMessage = '';

    setTab(tab: string) {
        this.activeTab = tab;
        this.message = '';
        this.importMessage = '';
        this.selectedFiles = undefined;
        this.currentFile = undefined;
    }

    importData() {
        this.progress = 0;
        this.importMessage = '';

        if (this.selectedFiles && this.selectedFiles.item(0)) {
            const file = this.selectedFiles.item(0);
            if (file) {
                this.currentFile = file;

                const uploadObs = this.importType === 'transactions'
                    ? this.documentService.importTransactions(file)
                    : this.documentService.importAssets(file);

                uploadObs.subscribe({
                    next: (res: any) => {
                        this.importMessage = 'Success: ' + (res || 'Data imported.');
                        this.currentFile = undefined;
                    },
                    error: (err: any) => {
                        console.error(err);
                        this.importMessage = 'Failed: ' + (err.error || err.message);
                        this.currentFile = undefined;
                    }
                });
            }
            this.selectedFiles = undefined;
        }
    }

    upload(): void {
        this.progress = 0;
        this.message = '';

        if (this.selectedFiles) {
            const file: File | null = this.selectedFiles.item(0);

            if (file) {
                this.currentFile = file;

                this.documentService.upload(this.currentFile).subscribe({
                    next: (event: any) => {
                        if (event.type === HttpEventType.UploadProgress) {
                            this.progress = Math.round(100 * event.loaded / event.total);
                        } else if (event instanceof HttpResponse) {
                            this.message = event.body.message;
                            this.fetchFiles();
                        }
                    },
                    error: (err: any) => {
                        console.log(err);
                        this.progress = 0;
                        if (err.error && err.error.message) {
                            this.message = err.error.message;
                        } else {
                            this.message = 'Could not upload the file!';
                        }
                        this.currentFile = undefined;
                    }
                });
            }
            this.selectedFiles = undefined;
        }
    }

    fetchFiles(): void {
        this.documentService.getFiles().subscribe(
            (data: any) => {
                this.fileInfos = data;
            },
            (err: any) => {
                console.log(err);
            }
        );
    }

    deleteFile(id: number): void {
        if (confirm("Are you sure you want to delete this file?")) {
            this.documentService.delete(id).subscribe({
                next: () => {
                    this.message = "File deleted successfully";
                    this.fetchFiles();
                },
                error: (err) => {
                    console.error(err);
                    this.message = "Could not delete file";
                }
            })
        }
    }

    viewFile(url: string): void {
        this.documentService.download(url).subscribe((blob: Blob) => {
            const objectUrl = URL.createObjectURL(blob);
            window.open(objectUrl, '_blank');
        }, error => {
            console.error("Download failed", error);
        });
    }
}
