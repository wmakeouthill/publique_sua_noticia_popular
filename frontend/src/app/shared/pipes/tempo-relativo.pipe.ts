import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'tempoRelativo', standalone: true })
export class TempoRelativoPipe implements PipeTransform {
  transform(value: string | Date | null | undefined): string {
    if (!value) return '';

    const data = new Date(value);
    if (isNaN(data.getTime())) return '';

    const agora = new Date();
    const diffMs = agora.getTime() - data.getTime();
    const diffSeg = Math.floor(diffMs / 1000);
    const diffMin = Math.floor(diffSeg / 60);
    const diffH = Math.floor(diffMin / 60);
    const diffDias = Math.floor(diffH / 24);
    const diffMeses = Math.floor(diffDias / 30);
    const diffAnos = Math.floor(diffDias / 365);

    if (diffSeg < 60) return 'agora';
    if (diffMin < 60) return `há ${diffMin} min`;
    if (diffH < 24) return `há ${diffH}h`;
    if (diffDias === 1) return 'ontem';
    if (diffDias < 30) return `há ${diffDias} dias`;
    if (diffMeses === 1) return 'há 1 mês';
    if (diffMeses < 12) return `há ${diffMeses} meses`;
    if (diffAnos === 1) return 'há 1 ano';
    return `há ${diffAnos} anos`;
  }
}
