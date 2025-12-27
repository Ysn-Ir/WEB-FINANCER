import { Component, ElementRef, OnInit, ViewChild, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ThemeService } from '../../../services/theme.service';

@Component({
  selector: 'app-particles',
  standalone: true,
  imports: [CommonModule],
  template: '<canvas #canvas></canvas>',
  styleUrl: './particles.component.css'
})
export class ParticlesComponent implements OnInit, OnDestroy {
  @ViewChild('canvas', { static: true }) canvasRef!: ElementRef<HTMLCanvasElement>;
  private ctx!: CanvasRenderingContext2D;
  private particles: Particle[] = [];
  private animationId: number = 0;
  private isDark = true;

  constructor(private themeService: ThemeService) { }

  ngOnInit() {
    this.themeService.isDarkTheme$.subscribe(dark => {
      this.isDark = dark;
      this.initParticles(); // Re-init colors
    });
    this.initCanvas();
    this.animate();
  }

  ngOnDestroy() {
    cancelAnimationFrame(this.animationId);
  }

  @HostListener('window:resize')
  onResize() {
    this.initCanvas();
  }

  private mouse = { x: -100, y: -100 };

  @HostListener('window:mousemove', ['$event'])
  onMouseMove(event: MouseEvent) {
    this.mouse.x = event.clientX;
    this.mouse.y = event.clientY;
  }

  private initCanvas() {
    const canvas = this.canvasRef.nativeElement;
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    this.ctx = canvas.getContext('2d')!;
    this.initParticles();
  }

  private initParticles() {
    this.particles = [];
    const count = Math.floor(window.innerWidth / 10); // Responsive count
    for (let i = 0; i < count; i++) {
      this.particles.push(new Particle(this.canvasRef.nativeElement.width, this.canvasRef.nativeElement.height));
    }
  }

  private animate() {
    this.ctx.clearRect(0, 0, this.canvasRef.nativeElement.width, this.canvasRef.nativeElement.height);

    // Update and Draw Particles
    this.particles.forEach(p => {
      p.update();
      // Draw Particle
      this.ctx.beginPath();
      this.ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
      this.ctx.fillStyle = this.isDark ? 'rgba(255, 255, 255, 0.8)' : 'rgba(0, 0, 0, 0.3)';
      this.ctx.fill();

      // Connections between particles
      this.particles.forEach(p2 => {
        const dx = p.x - p2.x;
        const dy = p.y - p2.y;
        const dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < 100) {
          this.ctx.beginPath();
          this.ctx.strokeStyle = this.isDark
            ? `rgba(255, 255, 255, ${0.15 - dist / 1000})`
            : `rgba(0, 0, 0, ${0.1 - dist / 1000})`;
          this.ctx.lineWidth = 0.5;
          this.ctx.moveTo(p.x, p.y);
          this.ctx.lineTo(p2.x, p2.y);
          this.ctx.stroke();
        }
      });

      // Connection to Mouse
      const dx = p.x - this.mouse.x;
      const dy = p.y - this.mouse.y;
      const dist = Math.sqrt(dx * dx + dy * dy);
      if (dist < 150) {
        this.ctx.beginPath();
        this.ctx.strokeStyle = this.isDark
          ? `rgba(99, 102, 241, ${0.5 - dist / 300})` // Indigo connection
          : `rgba(79, 70, 229, ${0.4 - dist / 300})`;
        this.ctx.lineWidth = 1;
        this.ctx.moveTo(p.x, p.y);
        this.ctx.lineTo(this.mouse.x, this.mouse.y);
        this.ctx.stroke();
      }
    });

    this.animationId = requestAnimationFrame(() => this.animate());
  }
}

class Particle {
  x: number;
  y: number;
  vx: number;
  vy: number;
  size: number;

  constructor(w: number, h: number) {
    this.x = Math.random() * w;
    this.y = Math.random() * h;
    this.vx = (Math.random() - 0.5) * 0.5;
    this.vy = (Math.random() - 0.5) * 0.5;
    this.size = Math.random() * 2.5; // Slightly larger for visibility
  }

  update() {
    this.x += this.vx;
    this.y += this.vy;

    // Bounce off edges
    if (this.x < 0 || this.x > window.innerWidth) this.vx *= -1;
    if (this.y < 0 || this.y > window.innerHeight) this.vy *= -1;
  }
}
